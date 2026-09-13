import { useState } from 'react';
import { useSemesters } from '../../api/semesterApi';
import { useUsers } from '../../api/userApi';
import { useSlotsBySemester, useAssignSlot, useReassignSlot } from '../../api/oeApi';
import Card from '../../shared/ui/Card';
import Button from '../../shared/ui/Button';
import { useAuth } from '../../hooks/useAuth';

const MODE_LABELS = {
  COMBINED: 'Combined Class Lecture (both divisions, one teacher)',
  SEPARATE_A: 'Separate — Division A',
  SEPARATE_B: 'Separate — Division B'
};

const MODES = ['COMBINED', 'SEPARATE_A', 'SEPARATE_B'];

export default function OeAccessPage() {
  const { userId } = useAuth();
  const semestersQuery = useSemesters();
  const teachersQuery = useUsers('TEACHER');

  const [semesterId, setSemesterId] = useState(null);
  const [pendingMode, setPendingMode] = useState(null);
  const [selectedTeacherId, setSelectedTeacherId] = useState('');
  const [error, setError] = useState('');
  const [reassigningSlotId, setReassigningSlotId] = useState(null);

  const slotsQuery = useSlotsBySemester(semesterId);
  const assignMutation = useAssignSlot();
  const reassignMutation = useReassignSlot();

  const activeSemesters = (semestersQuery.data ?? []).filter((s) => s.status === 'ACTIVE');
  const selectedSemester = activeSemesters.find((s) => s.id === semesterId);

  function slotForMode(mode) {
    return (slotsQuery.data ?? []).find((s) => s.mode === mode) ?? null;
  }

  async function handleAssign(mode) {
    setError('');
    if (!selectedTeacherId) {
      setError('Select a teacher first.');
      return;
    }
    try {
      await assignMutation.mutateAsync({
        semesterId,
        mode,
        teacherId: Number(selectedTeacherId)
      });
      setPendingMode(null);
      setSelectedTeacherId('');
    } catch (err) {
      // This is exactly the poster's "Cannot Assign" rejection UX — surfaced
      // as the backend's own message, not reworded, since it already names
      // who holds it and who can change it.
      setError(err.response?.data?.message || 'Could not assign this teaching slot.');
    }
  }

  async function handleReassign(slotId, mode) {
    setError('');
    if (!selectedTeacherId) {
      setError('Select a teacher first.');
      return;
    }
    try {
      await reassignMutation.mutateAsync({
        id: slotId,
        payload: { semesterId, mode, teacherId: Number(selectedTeacherId) }
      });
      setReassigningSlotId(null);
      setSelectedTeacherId('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reassign this teaching slot.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">OE Access</h1>
        <p className="text-sm text-gray-500 mt-1">
          Assign a teacher to each teaching mode for this semester. Only one teacher can hold a
          mode at a time — the first valid assignment locks it, and only you can reassign it later.
        </p>
      </div>

      <Card title="Select Semester">
        <select
          value={semesterId ?? ''}
          onChange={(e) => setSemesterId(e.target.value ? Number(e.target.value) : null)}
          className="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="">Select an active semester</option>
          {activeSemesters.map((s) => (
            <option key={s.id} value={s.id}>
              {s.label} ({s.classAName} + {s.classBName})
            </option>
          ))}
        </select>
      </Card>

      {error && (
        <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl px-4 py-3">
          {error}
        </div>
      )}

      {selectedSemester && (
        <div className="space-y-3">
          {MODES.map((mode) => {
            const slot = slotForMode(mode);
            const isAssigningThis = pendingMode === mode;
            const isReassigningThis = reassigningSlotId === slot?.id;

            return (
              <Card key={mode}>
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm font-semibold text-gray-900">{MODE_LABELS[mode]}</div>
                    {slot ? (
                      <div className="text-xs text-gray-500 mt-1">
                        Assigned to <span className="font-medium text-gray-700">{slot.teacherName}</span> by{' '}
                        {slot.assignedByName}
                        {slot.subjectCreated && (
                          <span className="ml-2 text-primary-600 font-medium">· Subject created</span>
                        )}
                      </div>
                    ) : (
                      <div className="text-xs text-gray-400 mt-1">Not assigned yet</div>
                    )}
                  </div>

                  {!slot && !isAssigningThis && (
                    <button
                      onClick={() => setPendingMode(mode)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-700"
                    >
                      Assign Teacher
                    </button>
                  )}

                  {slot && slot.assignedById === userId && !slot.subjectCreated && !isReassigningThis && (
                    <button
                      onClick={() => setReassigningSlotId(slot.id)}
                      className="text-xs font-medium text-gray-500 hover:text-primary-600"
                    >
                      Reassign
                    </button>
                  )}

                  {slot && slot.subjectCreated && (
                    <span className="text-xs text-gray-400">Locked — subject already created</span>
                  )}
                </div>

                {(isAssigningThis || isReassigningThis) && (
                  <div className="flex items-center gap-2 mt-3 pt-3 border-t border-gray-50">
                    <select
                      value={selectedTeacherId}
                      onChange={(e) => setSelectedTeacherId(e.target.value)}
                      className="rounded-lg border border-gray-200 px-2.5 py-1.5 text-xs"
                    >
                      <option value="">Select teacher</option>
                      {(teachersQuery.data ?? []).map((t) => (
                        <option key={t.id} value={t.id}>{t.fullName}</option>
                      ))}
                    </select>
                    <button
                      onClick={() =>
                        isReassigningThis ? handleReassign(slot.id, mode) : handleAssign(mode)
                      }
                      disabled={assignMutation.isPending || reassignMutation.isPending}
                      className="text-xs font-medium text-primary-600 hover:text-primary-700"
                    >
                      Confirm
                    </button>
                    <button
                      onClick={() => {
                        setPendingMode(null);
                        setReassigningSlotId(null);
                        setSelectedTeacherId('');
                      }}
                      className="text-xs font-medium text-gray-400 hover:text-gray-600"
                    >
                      Cancel
                    </button>
                  </div>
                )}
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}