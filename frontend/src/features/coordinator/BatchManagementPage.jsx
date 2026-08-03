import { useState } from 'react';
import { useClasses } from '../../api/classApi';
import { useSubjectsByClass } from '../../api/subjectApi';
import { useBatchesBySubject, useCreateBatch, useDeleteBatch } from '../../api/batchApi';
import Card from '../../shared/ui/Card';
import Button from '../../shared/ui/Button';

function SubjectBatchRow({ subject }) {
  const batchesQuery = useBatchesBySubject(subject.id);
  const createBatchMutation = useCreateBatch();
  const deleteBatchMutation = useDeleteBatch();
  const [label, setLabel] = useState('');

  if (subject.type !== 'PR') {
    return null;
  }

  return (
    <div className="border border-gray-100 rounded-xl p-3">
      <div className="font-medium text-gray-900 text-sm mb-2">{subject.name}</div>
      <div className="flex flex-wrap gap-2 mb-3">
        {(batchesQuery.data ?? []).map((b) => (
          <span key={b.id} className="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-700">
            {b.label}
            <button onClick={() => deleteBatchMutation.mutate(b.id)} className="text-gray-400 hover:text-red-600">×</button>
          </span>
        ))}
        {(batchesQuery.data ?? []).length === 0 && (
          <span className="text-xs text-gray-400">No batches yet</span>
        )}
      </div>
      <div className="flex gap-2">
        <input
          type="text"
          value={label}
          onChange={(e) => setLabel(e.target.value)}
          placeholder="e.g. B1"
          className="rounded-lg border border-gray-200 px-2.5 py-1.5 text-xs w-32"
        />
        <button
          onClick={() => {
            if (label) {
              createBatchMutation.mutate({ subjectId: subject.id, label });
              setLabel('');
            }
          }}
          className="text-xs font-medium text-primary-600 hover:text-primary-700"
        >
          Add Batch
        </button>
      </div>
    </div>
  );
}

export default function BatchManagementPage() {
  const classesQuery = useClasses();
  const [classId, setClassId] = useState(null);
  const subjectsQuery = useSubjectsByClass(classId);

  const practicalSubjects = (subjectsQuery.data ?? []).filter((s) => s.type === 'PR');

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Batch Management</h1>
        <p className="text-sm text-gray-500 mt-1">Manage batches across all Practical subjects in a class.</p>
      </div>

      <Card title="Select Class">
        <select
          value={classId ?? ''}
          onChange={(e) => setClassId(e.target.value ? Number(e.target.value) : null)}
          className="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="">Select a class</option>
          {(classesQuery.data ?? []).map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </Card>

      {classId && (
        <Card title="Practical Subjects & Batches">
          {subjectsQuery.isLoading ? (
            <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
          ) : practicalSubjects.length === 0 ? (
            <div className="text-sm text-gray-400 py-4 text-center">
              No Practical subjects in this class yet. Create one under Subjects.
            </div>
          ) : (
            <div className="space-y-3">
              {practicalSubjects.map((s) => (
                <SubjectBatchRow key={s.id} subject={s} />
              ))}
            </div>
          )}
        </Card>
      )}
    </div>
  );
}