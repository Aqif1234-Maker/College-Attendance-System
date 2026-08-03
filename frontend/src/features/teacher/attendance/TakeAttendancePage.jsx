import { useState, useEffect } from 'react';
import { CheckCircle2, XCircle, RotateCcw } from 'lucide-react';
import DependentFilter from '../../../shared/DependentFilter/DependentFilter';
import SlotSelector from '../../../shared/SlotSelector/SlotSelector';
import { useDependentFilter } from '../../../shared/DependentFilter/useDependentFilter';
import { useOpenAttendanceSession, useSubmitAttendance } from '../../../api/attendanceApi';
import Card from '../../../shared/ui/Card';
import Button from '../../../shared/ui/Button';

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

export default function TakeAttendancePage() {
  const filter = useDependentFilter();

  const [sessionDate, setSessionDate] = useState(todayIsoDate());
  const [slot, setSlot] = useState(null);
  const [session, setSession] = useState(null);
  const [statuses, setStatuses] = useState({});
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const openSessionMutation = useOpenAttendanceSession();
  const submitMutation = useSubmitAttendance();

  // Selecting a new subject changes its type (TH/PR), which changes the valid slot
  // range — reset the chosen slot so a stale slot number from the other type can't
  // be submitted (spec §4.2: "invalid combinations must never be selectable").
  useEffect(() => {
    setSlot(null);
    setSession(null);
  }, [filter.subjectId, filter.batchId]);

  const readyToOpen = filter.isComplete && Boolean(sessionDate) && Boolean(slot);

  async function handleOpenSession() {
    setError('');
    setSuccessMessage('');
    try {
      const opened = await openSessionMutation.mutateAsync({
        classId: filter.classId,
        subjectId: filter.subjectId,
        batchId: filter.batchId,
        sessionDate,
        slot
      });
      setSession(opened);

      const initialStatuses = {};
      opened.students.forEach((s) => {
        initialStatuses[s.studentId] = s.status ?? null;
      });
      setStatuses(initialStatuses);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not start this attendance session.');
    }
  }

  function setStudentStatus(studentId, status) {
    setStatuses((prev) => ({ ...prev, [studentId]: status }));
  }

  function markAllPresent() {
    if (!session) return;
    const next = {};
    session.students.forEach((s) => (next[s.studentId] = 'PRESENT'));
    setStatuses(next);
  }

  function markAllAbsent() {
    if (!session) return;
    const next = {};
    session.students.forEach((s) => (next[s.studentId] = 'ABSENT'));
    setStatuses(next);
  }

  function resetStatuses() {
    if (!session) return;
    const next = {};
    session.students.forEach((s) => (next[s.studentId] = null));
    setStatuses(next);
  }

  async function handleSubmit() {
    setError('');
    setSuccessMessage('');

    const unmarked = session.students.filter((s) => !statuses[s.studentId]);
    if (unmarked.length > 0) {
      setError(`${unmarked.length} student(s) are not yet marked Present or Absent.`);
      return;
    }

    try {
      await submitMutation.mutateAsync({
        sessionId: session.id,
        statuses: session.students.map((s) => ({
          studentId: s.studentId,
          status: statuses[s.studentId]
        }))
      });
      setSuccessMessage('Attendance submitted successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not submit attendance.');
    }
  }

  return (
    <div className="space-y-5 pb-24">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Take Attendance</h1>
        <p className="text-sm text-gray-500 mt-1">
          Select a class, subject, and batch you're assigned to, then choose a date and slot.
        </p>
      </div>

      <DependentFilter filter={filter} />

      <Card title="Date & Slot">
        <div className="space-y-4">
          <div className="max-w-xs">
            <label className="block text-xs font-medium text-gray-500 mb-1">Date</label>
            <input
              type="date"
              value={sessionDate}
              onChange={(e) => setSessionDate(e.target.value)}
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          {filter.selectedSubject && (
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-2">Slot</label>
              <SlotSelector subjectType={filter.selectedSubject.type} value={slot} onChange={setSlot} />
            </div>
          )}

          {!session && (
            <Button onClick={handleOpenSession} disabled={!readyToOpen} loading={openSessionMutation.isPending}>
              Start Attendance Session
            </Button>
          )}
        </div>
      </Card>

      {error && (
        <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl px-4 py-3">
          {error}
        </div>
      )}
      {successMessage && (
        <div className="text-sm text-green-700 bg-green-50 border border-green-100 rounded-xl px-4 py-3">
          {successMessage}
        </div>
      )}

      {session && (
        <Card
          title={`Students (${session.students.length})`}
          action={
            <div className="flex gap-2">
              <button
                onClick={markAllPresent}
                className="flex items-center gap-1.5 text-xs font-medium text-green-700 bg-green-50 hover:bg-green-100 px-3 py-1.5 rounded-lg"
              >
                <CheckCircle2 size={14} /> All Present
              </button>
              <button
                onClick={markAllAbsent}
                className="flex items-center gap-1.5 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 px-3 py-1.5 rounded-lg"
              >
                <XCircle size={14} /> All Absent
              </button>
              <button
                onClick={resetStatuses}
                className="flex items-center gap-1.5 text-xs font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded-lg"
              >
                <RotateCcw size={14} /> Reset
              </button>
            </div>
          }
        >
          <div className="rounded-xl border border-gray-100 overflow-auto max-h-[480px]">
            <table className="w-full text-sm">
              <thead className="sticky top-0 bg-gray-50 z-10">
                <tr>
                  <th className="text-left font-medium text-gray-500 px-4 py-3 border-b border-gray-100">Roll No</th>
                  <th className="text-left font-medium text-gray-500 px-4 py-3 border-b border-gray-100">Student Name</th>
                  <th className="text-left font-medium text-gray-500 px-4 py-3 border-b border-gray-100">Status</th>
                </tr>
              </thead>
              <tbody>
                {session.students.map((s, i) => (
                  <tr key={s.studentId} className={i % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'}>
                    <td className="px-4 py-3 border-b border-gray-50 text-gray-700">{s.rollNo}</td>
                    <td className="px-4 py-3 border-b border-gray-50 text-gray-700">{s.studentName}</td>
                    <td className="px-4 py-3 border-b border-gray-50">
                      <div className="flex gap-4">
                        <label className="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer">
                          <input
                            type="radio"
                            name={`status-${s.studentId}`}
                            checked={statuses[s.studentId] === 'PRESENT'}
                            onChange={() => setStudentStatus(s.studentId, 'PRESENT')}
                            className="accent-primary-600"
                          />
                          Present
                        </label>
                        <label className="flex items-center gap-1.5 text-sm text-gray-700 cursor-pointer">
                          <input
                            type="radio"
                            name={`status-${s.studentId}`}
                            checked={statuses[s.studentId] === 'ABSENT'}
                            onChange={() => setStudentStatus(s.studentId, 'ABSENT')}
                            className="accent-primary-600"
                          />
                          Absent
                        </label>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {session && (
        <div className="fixed bottom-0 left-60 right-0 bg-white border-t border-gray-100 px-6 py-4 flex justify-end">
          <Button onClick={handleSubmit} loading={submitMutation.isPending}>
            Submit Attendance
          </Button>
        </div>
      )}
    </div>
  );
}