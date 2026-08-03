import { useEffect, useMemo, useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { useUsers } from '../../api/userApi';
import { useSubjectsByClass } from '../../api/subjectApi';
import { useBatchesBySubject } from '../../api/batchApi';
import { useAssignmentsForClass, useAssignTeacher, useRemoveAssignment } from '../../api/assignmentApi';
import { useCoordinatorClasses } from '../../hooks/useCoordinatorClasses';

function AssignmentRow({ classId, subject, batch, assignment, teachers }) {
  const [teacherId, setTeacherId] = useState(assignment?.teacherId ?? '');
  const [error, setError] = useState('');
  const assignMutation = useAssignTeacher();
  const removeMutation = useRemoveAssignment();

  useEffect(() => {
    setTeacherId(assignment?.teacherId ?? '');
  }, [assignment?.teacherId]);

  async function handleAssign() {
    setError('');
    if (!teacherId) {
      setError('Select a teacher first.');
      return;
    }

    try {
      await assignMutation.mutateAsync({
        teacherId: Number(teacherId),
        classId,
        subjectId: subject.id,
        batchId: batch.id
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not assign teacher.');
    }
  }

  async function handleUnassign() {
    setError('');
    if (!assignment?.id) return;

    try {
      await removeMutation.mutateAsync(assignment.id);
      setTeacherId('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not remove assignment.');
    }
  }

  const isAssigned = Boolean(assignment);
  const selectedChanged = isAssigned && Number(teacherId) !== assignment.teacherId;

  return (
    <div className="space-y-2">
      <div className="grid gap-3 md:grid-cols-[80px_1fr_auto] md:items-center">
        <div className="text-base font-semibold text-gray-900">
          {subject.type === 'TH' ? '' : batch.label}
        </div>

        <div className="relative">
          <select
            value={teacherId}
            onChange={(e) => setTeacherId(e.target.value ? Number(e.target.value) : '')}
            className="w-full appearance-none rounded-xl border border-transparent bg-white px-3 py-2 pr-9 text-base text-gray-900 focus:border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">Select teacher</option>
            {teachers.map((teacher) => (
              <option key={teacher.id} value={teacher.id}>
                {teacher.fullName}{teacher.role === 'CLASS_COORDINATOR' ? ' (Coordinator)' : ''}
              </option>
            ))}
          </select>
          <ChevronDown
            size={18}
            className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-700"
          />
        </div>

        <div className="flex items-center gap-4 md:justify-end">
          {isAssigned && (
            <span className="text-sm font-semibold text-emerald-600">Assigned</span>
          )}
          {(!isAssigned || selectedChanged) && (
            <button
              onClick={handleAssign}
              disabled={assignMutation.isPending}
              className="text-sm font-semibold text-primary-600 hover:text-primary-700 disabled:opacity-60"
            >
              {selectedChanged ? 'Update' : 'Assign'}
            </button>
          )}
          {isAssigned && (
            <button
              onClick={handleUnassign}
              disabled={removeMutation.isPending}
              className="text-sm font-semibold text-red-600 hover:text-red-700 disabled:opacity-60"
            >
              Unassign
            </button>
          )}
        </div>
      </div>

      {error && <div className="text-sm font-medium text-red-600">{error}</div>}
    </div>
  );
}

function SubjectAssignmentCard({ classId, subject, teachers, assignments }) {
  const [showBatches, setShowBatches] = useState(subject.type === 'TH');
  const batchesQuery = useBatchesBySubject(subject.id);

  const visibleBatches = useMemo(() => {
    const batches = batchesQuery.data ?? [];
    if (subject.type === 'TH') {
      const wholeClassBatch = batches.find((batch) => batch.wholeClass);
      return wholeClassBatch ? [wholeClassBatch] : batches.slice(0, 1);
    }
    return batches.filter((batch) => !batch.wholeClass);
  }, [batchesQuery.data, subject.type]);

  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <h2 className="text-xl font-bold text-gray-800">
          {subject.name}{' '}
          <span className="text-sm font-bold text-gray-500">({subject.type})</span>
        </h2>
        {subject.type === 'PR' && (
          <button
            onClick={() => setShowBatches((value) => !value)}
            className="text-sm font-semibold text-primary-600 hover:text-primary-700"
          >
            {showBatches ? 'Hide batches' : 'Load batches'}
          </button>
        )}
      </div>

      {subject.type === 'TH' && (
        <div className="mt-4">
          {batchesQuery.isLoading ? (
            <div className="text-sm text-gray-400">Loading assignment slot...</div>
          ) : visibleBatches.length === 0 ? (
            <div className="text-sm text-red-600">This theory subject is missing its Entire Class batch.</div>
          ) : (
            <AssignmentRow
              classId={classId}
              subject={subject}
              batch={visibleBatches[0]}
              assignment={assignments.find((item) => item.subjectId === subject.id && item.batchId === visibleBatches[0].id)}
              teachers={teachers}
            />
          )}
        </div>
      )}

      {subject.type === 'PR' && showBatches && (
        <div className="mt-4 space-y-3">
          {batchesQuery.isLoading ? (
            <div className="text-sm text-gray-400">Loading batches...</div>
          ) : visibleBatches.length === 0 ? (
            <div className="text-sm text-gray-400">No batches yet. Add batches from Subjects & Batches first.</div>
          ) : (
            visibleBatches.map((batch) => (
              <AssignmentRow
                key={batch.id}
                classId={classId}
                subject={subject}
                batch={batch}
                assignment={assignments.find((item) => item.subjectId === subject.id && item.batchId === batch.id)}
                teachers={teachers}
              />
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default function FacultyAssignmentPage() {
  const teachersQuery = useUsers('TEACHER');
  const { classes, isLoading: classesLoading } = useCoordinatorClasses();
  const [classId, setClassId] = useState(null);

  const subjectsQuery = useSubjectsByClass(classId);
  const assignmentsQuery = useAssignmentsForClass(classId);

  useEffect(() => {
    if (classes.length === 0) {
      setClassId(null);
      return;
    }

    if (!classId || !classes.some((classItem) => classItem.id === classId)) {
      setClassId(classes[0].id);
    }
  }, [classes, classId]);

  const teachers = teachersQuery.data ?? [];
  const assignments = assignmentsQuery.data ?? [];

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-gray-800">Faculty Assignment</h1>
        <div className="mt-5">
          <select
            value={classId ?? ''}
            onChange={(e) => setClassId(e.target.value ? Number(e.target.value) : null)}
            className="w-48 rounded-xl border border-gray-200 bg-white px-3 py-2 text-base font-medium text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">Select class</option>
            {classes.map((classItem) => (
              <option key={classItem.id} value={classItem.id}>
                {classItem.name}
              </option>
            ))}
          </select>
          {!classesLoading && classes.length === 0 && (
            <p className="mt-3 text-sm text-gray-500">
              You are not assigned as coordinator for any class in the current academic year.
            </p>
          )}
        </div>
      </div>

      {classId && (
        <div className="space-y-5">
          {subjectsQuery.isLoading || assignmentsQuery.isLoading || teachersQuery.isLoading ? (
            <div className="rounded-2xl bg-white p-8 text-center text-sm text-gray-400">
              Loading faculty assignments...
            </div>
          ) : (subjectsQuery.data ?? []).length === 0 ? (
            <div className="rounded-2xl bg-white p-8 text-center text-sm text-gray-400">
              No subjects yet for this class.
            </div>
          ) : (
            subjectsQuery.data.map((subject) => (
              <SubjectAssignmentCard
                key={subject.id}
                classId={classId}
                subject={subject}
                teachers={teachers}
                assignments={assignments}
              />
            ))
          )}
        </div>
      )}
    </div>
  );
}
