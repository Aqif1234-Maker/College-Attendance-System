import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMySlots, useOeSubjectBySlot, useCreateOeSubject } from '../../../api/oeApi';
import { useAuth } from '../../../hooks/useAuth';
import Card from '../../../shared/ui/Card';
import Button from '../../../shared/ui/Button';

const MODE_LABELS = {
  COMBINED: 'Combined Class Lecture (both divisions)',
  SEPARATE_A: 'Separate — Division A',
  SEPARATE_B: 'Separate — Division B'
};

function SlotRow({ slot }) {
  const navigate = useNavigate();
  const oeSubjectQuery = useOeSubjectBySlot(slot.id);
  const createMutation = useCreateOeSubject();

  const [name, setName] = useState('');
  const [error, setError] = useState('');

  // A 404 here means "no subject created yet" — the expected, normal state for
  // an ungranted slot, not a real error. Only a non-404 failure is a genuine problem.
  const hasNoSubjectYet = oeSubjectQuery.isError && oeSubjectQuery.error?.response?.status === 404;
  const hasSubject = oeSubjectQuery.isSuccess && oeSubjectQuery.data;

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    try {
      await createMutation.mutateAsync({ oeTeachingSlotId: slot.id, name });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create subject.');
    }
  }

  function goToEnroll(oeSubjectId, subjectName) {
    // semesterId comes from the slot itself, not the subject response — the enroll
    // page needs it to look up classAId/classBId and can't derive it any other way
    // without an extra backend endpoint (see fragility note on EnrollStudentsPage).
    navigate(`/teacher/oe/${oeSubjectId}/enroll`, {
      state: {
        subjectName,
        mode: slot.mode,
        semesterId: slot.semesterId
      }
    });
  }

  return (
    <Card>
      <div className="flex items-center justify-between mb-3">
        <div>
          <div className="text-sm font-semibold text-gray-900">{slot.semesterLabel}</div>
          <div className="text-xs text-gray-500 mt-0.5">{MODE_LABELS[slot.mode]}</div>
        </div>
        {hasSubject && (
          <span className="text-xs font-medium text-primary-700 bg-primary-50 px-2.5 py-1 rounded-full">
            Subject created
          </span>
        )}
      </div>

      {oeSubjectQuery.isLoading && (
        <div className="text-sm text-gray-400 py-2">Checking…</div>
      )}

      {hasSubject && (
        <div className="flex items-center justify-between pt-3 border-t border-gray-50">
          <span className="text-sm text-gray-700 font-medium">{oeSubjectQuery.data.subjectName}</span>
          <button
            onClick={() => goToEnroll(oeSubjectQuery.data.id, oeSubjectQuery.data.subjectName)}
            className="text-xs font-medium text-primary-600 hover:text-primary-700"
          >
            Manage Enrollment →
          </button>
        </div>
      )}

      {hasNoSubjectYet && !createMutation.isSuccess && (
        <form onSubmit={handleCreate} className="flex gap-2 pt-3 border-t border-gray-50">
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. FinTech"
            required
            className="flex-1 rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
          <Button type="submit" loading={createMutation.isPending}>
            Create Subject
          </Button>
        </form>
      )}

      {createMutation.isSuccess && (
        <div className="pt-3 border-t border-gray-50 flex items-center justify-between">
          <span className="text-sm text-gray-700 font-medium">{createMutation.data.subjectName}</span>
          <button
            onClick={() => goToEnroll(createMutation.data.id, createMutation.data.subjectName)}
            className="text-xs font-medium text-primary-600 hover:text-primary-700"
          >
            Manage Enrollment →
          </button>
        </div>
      )}

      {error && <div className="text-sm text-red-600 mt-2">{error}</div>}
    </Card>
  );
}

export default function CreateOeSubjectPage() {
  const { fullName } = useAuth();
  const mySlotsQuery = useMySlots();

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">My Open Elective Subjects</h1>
        <p className="text-sm text-gray-500 mt-1">
          {fullName}, here are the teaching modes your Coordinator has assigned you.
        </p>
      </div>

      {mySlotsQuery.isLoading ? (
        <div className="text-sm text-gray-400 py-8 text-center">Loading…</div>
      ) : (mySlotsQuery.data ?? []).length === 0 ? (
        <div className="text-sm text-gray-400 py-8 text-center bg-white rounded-2xl border border-gray-100">
          You have not been assigned an Open Elective teaching slot yet. Contact your Coordinator.
        </div>
      ) : (
        <div className="space-y-3">
          {mySlotsQuery.data.map((slot) => (
            <SlotRow key={slot.id} slot={slot} />
          ))}
        </div>
      )}
    </div>
  );
}