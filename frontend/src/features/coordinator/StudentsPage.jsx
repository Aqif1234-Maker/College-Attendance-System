import { useEffect, useState } from 'react';
import { useStudentsByClass, useCreateStudent, useDeactivateStudent } from '../../api/studentApi';
import { useCoordinatorClasses } from '../../hooks/useCoordinatorClasses';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';
import Button from '../../shared/ui/Button';

export default function StudentsPage() {
  const { classes, isLoading: classesLoading } = useCoordinatorClasses();
  const [classId, setClassId] = useState(null);

  const studentsQuery = useStudentsByClass(classId);
  const createStudentMutation = useCreateStudent();
  const deactivateMutation = useDeactivateStudent();

  const [form, setForm] = useState({ rollNo: '', name: '', batchLabel: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (classes.length === 0) {
      setClassId(null);
      return;
    }

    if (!classId || !classes.some((classItem) => classItem.id === classId)) {
      setClassId(classes[0].id);
    }
  }, [classes, classId]);

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    if (!classId) {
      setError('Select a class first.');
      return;
    }
    try {
      await createStudentMutation.mutateAsync({
        classId,
        rollNo: form.rollNo,
        name: form.name,
        batchLabel: form.batchLabel || null
      });
      setForm({ rollNo: '', name: '', batchLabel: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add student.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Students</h1>
        <p className="text-sm text-gray-500 mt-1">Add students to your classes, organized by batch.</p>
      </div>

      <Card title="Select Class">
        <select
          value={classId ?? ''}
          onChange={(e) => setClassId(e.target.value ? Number(e.target.value) : null)}
          className="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="">Select a class</option>
          {classes.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        {!classesLoading && classes.length === 0 && (
          <p className="mt-3 text-sm text-gray-500">
            You are not assigned as coordinator for any class in the current academic year.
          </p>
        )}
      </Card>

      {classId && (
        <Card title="Add Student">
          <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Roll No</label>
              <input
                type="text"
                value={form.rollNo}
                onChange={(e) => setForm({ ...form, rollNo: e.target.value })}
                required
                className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Name</label>
              <input
                type="text"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
                className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Batch (optional)</label>
              <input
                type="text"
                value={form.batchLabel}
                onChange={(e) => setForm({ ...form, batchLabel: e.target.value })}
                placeholder="e.g. B1"
                className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>
            <Button type="submit" loading={createStudentMutation.isPending}>Add Student</Button>
          </form>
          {error && <div className="text-sm text-red-600 mt-3">{error}</div>}
        </Card>
      )}

      {classId && (
        <Card title="Students">
          <Table
            columns={[
              { key: 'rollNo', header: 'Roll No' },
              { key: 'name', header: 'Name' },
              { key: 'batchLabel', header: 'Batch', render: (row) => row.batchLabel || '—' },
              {
                key: 'active',
                header: 'Status',
                render: (row) =>
                  row.active ? (
                    <button
                      onClick={() => deactivateMutation.mutate(row.id)}
                      className="text-xs font-medium text-green-700 bg-green-50 px-2.5 py-1 rounded-full hover:bg-red-50 hover:text-red-600"
                    >
                      Active
                    </button>
                  ) : (
                    <span className="text-xs font-medium text-gray-400 bg-gray-100 px-2.5 py-1 rounded-full">
                      Inactive
                    </span>
                  )
              }
            ]}
            rows={(studentsQuery.data ?? []).map((s) => ({ ...s, id: s.id }))}
            emptyMessage="No students added yet."
          />
        </Card>
      )}
    </div>
  );
}
