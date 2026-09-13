import { useState } from 'react';
import { useCurrentAcademicYear } from '../../api/academicYearApi';
import { useClasses } from '../../api/classApi';
import { useSemesters, useCreateSemester, useCloseSemester } from '../../api/semesterApi';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';
import Button from '../../shared/ui/Button';
import Badge from '../../shared/ui/Badge';

export default function SemestersPage() {
  const currentYearQuery = useCurrentAcademicYear();
  const classesQuery = useClasses();
  const semestersQuery = useSemesters();

  const [form, setForm] = useState({ label: '', classAId: '', classBId: '' });
  const [error, setError] = useState('');

  const createMutation = useCreateSemester();
  const closeMutation = useCloseSemester();

  async function handleCreate(e) {
    e.preventDefault();
    setError('');

    if (!currentYearQuery.data) {
      setError('No current academic year is set. Set one under Academic Year first.');
      return;
    }
    if (form.classAId === form.classBId) {
      setError('Division A and Division B must be different classes.');
      return;
    }

    try {
      await createMutation.mutateAsync({
        academicYearId: currentYearQuery.data.id,
        label: form.label,
        classAId: Number(form.classAId),
        classBId: Number(form.classBId)
      });
      setForm({ label: '', classAId: '', classBId: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create semester.');
    }
  }

  async function handleClose(id) {
    setError('');
    try {
      await closeMutation.mutateAsync(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not close semester.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Semesters</h1>
        <p className="text-sm text-gray-500 mt-1">
          Open Elective setup is scoped to a semester with two divisions (e.g. TE-A, TE-B).
          Once closed, a semester's setup becomes permanent, read-only history.
        </p>
      </div>

      <Card title="Open New Semester">
        <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Label</label>
            <input
              type="text"
              value={form.label}
              onChange={(e) => setForm({ ...form, label: e.target.value })}
              placeholder="e.g. TE Sem 1 2025-26"
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Division A</label>
            <select
              value={form.classAId}
              onChange={(e) => setForm({ ...form, classAId: e.target.value })}
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="">Select class</option>
              {(classesQuery.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Division B</label>
            <select
              value={form.classBId}
              onChange={(e) => setForm({ ...form, classBId: e.target.value })}
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="">Select class</option>
              {(classesQuery.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <Button type="submit" loading={createMutation.isPending}>Open Semester</Button>
        </form>
        {error && <div className="text-sm text-red-600 mt-3">{error}</div>}
      </Card>

      <Card title="All Semesters">
        {semestersQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : (
          <Table
            columns={[
              { key: 'label', header: 'Semester' },
              { key: 'academicYearLabel', header: 'Academic Year' },
              {
                key: 'divisions',
                header: 'Divisions',
                render: (row) => `${row.classAName} + ${row.classBName}`
              },
              {
                key: 'status',
                header: 'Status',
                render: (row) =>
                  row.status === 'CLOSED' ? (
                    <Badge label="Closed" />
                  ) : (
                    <span className="text-xs font-medium text-green-700 bg-green-50 px-2.5 py-1 rounded-full">
                      Active
                    </span>
                  )
              },
              {
                key: 'action',
                header: '',
                render: (row) =>
                  row.status === 'ACTIVE' ? (
                    <button
                      onClick={() => handleClose(row.id)}
                      disabled={closeMutation.isPending}
                      className="text-xs font-medium text-red-600 hover:text-red-700"
                    >
                      Close Semester
                    </button>
                  ) : (
                    <span className="text-xs text-gray-400">Preserved as history</span>
                  )
              }
            ]}
            rows={(semestersQuery.data ?? []).map((s) => ({ ...s, id: s.id }))}
            emptyMessage="No semesters created yet."
          />
        )}
      </Card>
    </div>
  );
}