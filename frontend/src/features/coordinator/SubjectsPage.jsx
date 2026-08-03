import { useEffect, useState } from 'react';
import { useSubjectsByClass, useCreateSubject, useDeleteSubject } from '../../api/subjectApi';
import { useBatchesBySubject, useCreateBatch, useDeleteBatch } from '../../api/batchApi';
import { useCoordinatorClasses } from '../../hooks/useCoordinatorClasses';
import Button from '../../shared/ui/Button';

function SubjectBatchCard({ subject, onDelete }) {
  const [expanded, setExpanded] = useState(subject.type === 'TH');
  const [label, setLabel] = useState('');
  const [error, setError] = useState('');

  const batchesQuery = useBatchesBySubject(subject.type === 'PR' ? subject.id : null);
  const createBatchMutation = useCreateBatch();
  const deleteBatchMutation = useDeleteBatch();

  async function handleAddBatch(e) {
    e.preventDefault();
    setError('');
    if (!label.trim()) return;

    try {
      await createBatchMutation.mutateAsync({ subjectId: subject.id, label: label.trim() });
      setLabel('');
      setExpanded(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add batch.');
    }
  }

  return (
    <div className="rounded-2xl bg-white border border-gray-100 px-5 py-4 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <h2 className="text-base font-semibold text-gray-800">
          {subject.name}{' '}
          <span className="text-xs font-semibold text-gray-500">
            ({subject.type})
          </span>
        </h2>
        <button
          onClick={onDelete}
          className="text-sm font-medium text-red-600 hover:text-red-700"
        >
          Delete
        </button>
      </div>

      {subject.type === 'PR' && (
        <div className="mt-4 border-l-2 border-gray-100 pl-4">
          <button
            onClick={() => setExpanded((value) => !value)}
            className="text-sm font-semibold text-primary-600 hover:text-primary-700"
          >
            {expanded ? 'Hide batches' : 'Load batches'}
          </button>

          {expanded && (
            <div className="mt-3 space-y-3">
              <div className="flex flex-wrap gap-2">
                {batchesQuery.isLoading && (
                  <span className="text-sm text-gray-400">Loading batches...</span>
                )}
                {(batchesQuery.data ?? []).map((batch) => (
                  <span
                    key={batch.id}
                    className="inline-flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1 text-sm font-medium text-gray-700"
                  >
                    {batch.label}
                    <button
                      onClick={() => deleteBatchMutation.mutate(batch.id)}
                      className="text-gray-400 hover:text-red-600"
                      aria-label={`Delete ${batch.label}`}
                    >
                      x
                    </button>
                  </span>
                ))}
                {!batchesQuery.isLoading && (batchesQuery.data ?? []).length === 0 && (
                  <span className="text-sm text-gray-400">No batches yet.</span>
                )}
              </div>

              <form onSubmit={handleAddBatch} className="flex flex-wrap items-center gap-3">
                <input
                  type="text"
                  value={label}
                  onChange={(e) => setLabel(e.target.value)}
                  placeholder="New batch e.g. B1"
                  className="w-44 rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
                />
                <button
                  type="submit"
                  disabled={createBatchMutation.isPending}
                  className="text-sm font-semibold text-primary-600 hover:text-primary-700 disabled:opacity-60"
                >
                  Add Batch
                </button>
              </form>
              {error && <div className="text-sm text-red-600">{error}</div>}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function SubjectsPage() {
  const { classes, isLoading: classesLoading } = useCoordinatorClasses();
  const [classId, setClassId] = useState(null);
  const [form, setForm] = useState({ name: '', type: 'TH' });
  const [error, setError] = useState('');

  const subjectsQuery = useSubjectsByClass(classId);
  const createSubjectMutation = useCreateSubject();
  const deleteSubjectMutation = useDeleteSubject();

  useEffect(() => {
    if (classes.length === 0) {
      setClassId(null);
      return;
    }

    if (!classId || !classes.some((classItem) => classItem.id === classId)) {
      setClassId(classes[0].id);
    }
  }, [classes, classId]);

  async function handleCreateSubject(e) {
    e.preventDefault();
    setError('');

    if (!classId) {
      setError('Select a class first.');
      return;
    }

    try {
      await createSubjectMutation.mutateAsync({
        classId,
        name: form.name.trim(),
        type: form.type
      });
      setForm({ name: '', type: 'TH' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create subject.');
    }
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-gray-800">Subjects & Batches</h1>
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
        <form
          onSubmit={handleCreateSubject}
          className="flex flex-col gap-4 rounded-2xl border border-gray-100 bg-white p-5 shadow-sm sm:flex-row sm:items-center"
        >
          <input
            type="text"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="Subject Name"
            required
            className="min-w-0 flex-1 rounded-xl border border-transparent px-3 py-3 text-base text-gray-800 placeholder:text-gray-400 focus:border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
          <select
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
            className="rounded-xl border border-transparent bg-white px-3 py-3 text-base text-gray-900 focus:border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="TH">Theory</option>
            <option value="PR">Practical</option>
          </select>
          <Button type="submit" loading={createSubjectMutation.isPending} className="px-6">
            Add
          </Button>
        </form>
      )}

      {error && <div className="text-sm font-medium text-red-600">{error}</div>}

      {classId && (
        <div className="space-y-4">
          {subjectsQuery.isLoading ? (
            <div className="rounded-2xl bg-white p-8 text-center text-sm text-gray-400">
              Loading subjects...
            </div>
          ) : (subjectsQuery.data ?? []).length === 0 ? (
            <div className="rounded-2xl bg-white p-8 text-center text-sm text-gray-400">
              No subjects yet for this class.
            </div>
          ) : (
            subjectsQuery.data.map((subject) => (
              <SubjectBatchCard
                key={subject.id}
                subject={subject}
                onDelete={() => deleteSubjectMutation.mutate(subject.id)}
              />
            ))
          )}
        </div>
      )}
    </div>
  );
}
