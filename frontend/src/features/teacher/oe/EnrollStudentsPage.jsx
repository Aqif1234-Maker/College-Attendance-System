import { useState } from 'react';
import { useParams, useLocation, Link } from 'react-router-dom';
import { useSemester } from '../../../api/semesterApi';
import { useStudentsByClass } from '../../../api/studentApi';
import { useEnrollments, useEnrollStudents, useUnenrollStudent } from '../../../api/oeApi';
import Card from '../../../shared/ui/Card';
import Button from '../../../shared/ui/Button';

export default function EnrollStudentsPage() {
  const { oeSubjectId } = useParams();
  const location = useLocation();
  const { subjectName, mode, semesterId } = location.state ?? {};

  const [selectedIds, setSelectedIds] = useState([]);
  const [error, setError] = useState('');

  const semesterQuery = useSemester(semesterId);
  const enrollmentsQuery = useEnrollments(Number(oeSubjectId));
  const enrollMutation = useEnrollStudents();
  const unenrollMutation = useUnenrollStudent();

  // Combined mode pulls from both divisions; Separate pulls from just one.
  const classAId = semesterQuery.data?.classAId;
  const classBId = semesterQuery.data?.classBId;

  const showClassA = mode === 'COMBINED' || mode === 'SEPARATE_A';
  const showClassB = mode === 'COMBINED' || mode === 'SEPARATE_B';

  const classAStudentsQuery = useStudentsByClass(showClassA ? classAId : null);
  const classBStudentsQuery = useStudentsByClass(showClassB ? classBId : null);

  if (!location.state) {
    return (
      <div className="bg-white rounded-2xl border border-gray-100 p-6 text-sm text-gray-500">
        This page needs to be opened from your OE Subjects list, not linked to directly.{' '}
        <Link to="/teacher/oe" className="text-primary-600 font-medium">Go back</Link>
      </div>
    );
  }

  const enrolledIds = new Set((enrollmentsQuery.data ?? []).map((e) => e.studentId));

  const candidateStudents = [
    ...(classAStudentsQuery.data ?? []).map((s) => ({ ...s, divisionLabel: semesterQuery.data?.classAName })),
    ...(classBStudentsQuery.data ?? []).map((s) => ({ ...s, divisionLabel: semesterQuery.data?.classBName }))
  ].filter((s) => !enrolledIds.has(s.id));

  function toggleSelected(studentId) {
    setSelectedIds((prev) =>
      prev.includes(studentId) ? prev.filter((id) => id !== studentId) : [...prev, studentId]
    );
  }

  async function handleEnroll() {
    setError('');
    if (selectedIds.length === 0) {
      setError('Select at least one student.');
      return;
    }
    try {
      await enrollMutation.mutateAsync({ oeSubjectId: Number(oeSubjectId), studentIds: selectedIds });
      setSelectedIds([]);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not enroll students.');
    }
  }

  async function handleUnenroll(studentId) {
    try {
      await unenrollMutation.mutateAsync({ oeSubjectId: Number(oeSubjectId), studentId });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not remove student.');
    }
  }

  const isLoading = semesterQuery.isLoading || classAStudentsQuery.isLoading || classBStudentsQuery.isLoading;

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">{subjectName}</h1>
        <p className="text-sm text-gray-500 mt-1">
          Enroll students from {showClassA && showClassB
            ? `${semesterQuery.data?.classAName} and ${semesterQuery.data?.classBName}`
            : semesterQuery.data?.[showClassA ? 'classAName' : 'classBName']}{' '}
          — existing students only, never newly created here.
        </p>
      </div>

      {error && (
        <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl px-4 py-3">{error}</div>
      )}

      <Card title={`Currently Enrolled (${enrollmentsQuery.data?.length ?? 0})`}>
        {enrollmentsQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : (enrollmentsQuery.data ?? []).length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">No students enrolled yet.</div>
        ) : (
          <div className="divide-y divide-gray-50">
            {enrollmentsQuery.data.map((e) => (
              <div key={e.studentId} className="flex items-center justify-between py-2.5 text-sm">
                <span className="text-gray-700">
                  {e.rollNo} · {e.studentName} · <span className="text-gray-400">{e.studentClassName}</span>
                </span>
                <button
                  onClick={() => handleUnenroll(e.studentId)}
                  className="text-xs font-medium text-red-600 hover:text-red-700"
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </Card>

      <Card
        title="Add Students"
        action={
          <Button onClick={handleEnroll} disabled={selectedIds.length === 0} loading={enrollMutation.isPending}>
            Enroll Selected ({selectedIds.length})
          </Button>
        }
      >
        {isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : candidateStudents.length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">
            No more students available to enroll from the eligible division(s).
          </div>
        ) : (
          <div className="max-h-[420px] overflow-auto divide-y divide-gray-50">
            {candidateStudents.map((s) => (
              <label key={s.id} className="flex items-center gap-3 py-2.5 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  checked={selectedIds.includes(s.id)}
                  onChange={() => toggleSelected(s.id)}
                  className="accent-primary-600"
                />
                <span className="text-gray-700">
                  {s.rollNo} · {s.name} · <span className="text-gray-400">{s.divisionLabel}</span>
                </span>
              </label>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}