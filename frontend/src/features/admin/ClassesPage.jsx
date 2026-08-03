import { useState } from 'react';
import { useCurrentAcademicYear } from '../../api/academicYearApi';
import { useClasses, useCreateClass, useAssignCoordinator } from '../../api/classApi';
import { useUsers } from '../../api/userApi';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';
import Button from '../../shared/ui/Button';

export default function ClassesPage() {
  const currentYearQuery = useCurrentAcademicYear();
  const classesQuery = useClasses(currentYearQuery.data?.id);
  const coordinatorsQuery = useUsers('CLASS_COORDINATOR');

  const [className, setClassName] = useState('');
  const [error, setError] = useState('');
  const [assigningClassId, setAssigningClassId] = useState(null);
  const [selectedCoordinatorId, setSelectedCoordinatorId] = useState('');

  const createClassMutation = useCreateClass();
  const assignCoordinatorMutation = useAssignCoordinator();

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    if (!currentYearQuery.data) {
      setError('No current academic year is set. Set one under Academic Year first.');
      return;
    }
    try {
      await createClassMutation.mutateAsync({
        academicYearId: currentYearQuery.data.id,
        name: className
      });
      setClassName('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create class.');
    }
  }

  async function handleAssignCoordinator(classId) {
    if (!selectedCoordinatorId) return;
    setError('');
    try {
      await assignCoordinatorMutation.mutateAsync({ classId, userId: Number(selectedCoordinatorId) });
      setAssigningClassId(null);
      setSelectedCoordinatorId('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not assign coordinator.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Classes</h1>
        <p className="text-sm text-gray-500 mt-1">
          Current year: {currentYearQuery.data?.label ?? 'None set'}
        </p>
      </div>

      <Card title="Add Class">
        <form onSubmit={handleCreate} className="flex gap-3 items-end">
          <div className="flex-1 max-w-xs">
            <label className="block text-xs font-medium text-gray-500 mb-1">Class Name</label>
            <input
              type="text"
              value={className}
              onChange={(e) => setClassName(e.target.value)}
              placeholder="e.g. SE-A"
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <Button type="submit" loading={createClassMutation.isPending}>Add Class</Button>
        </form>
        {error && <div className="text-sm text-red-600 mt-3">{error}</div>}
      </Card>

      <Card title="All Classes">
        <Table
          columns={[
            { key: 'name', header: 'Class' },
            { key: 'academicYearLabel', header: 'Academic Year' },
            {
              key: 'coordinator',
              header: 'Coordinator',
              render: (row) =>
                row.coordinatorName ? (
                  <span className="text-gray-700">{row.coordinatorName}</span>
                ) : assigningClassId === row.id ? (
                  <div className="flex items-center gap-2">
                    <select
                      value={selectedCoordinatorId}
                      onChange={(e) => setSelectedCoordinatorId(e.target.value)}
                      className="rounded-lg border border-gray-200 px-2 py-1 text-xs"
                    >
                      <option value="">Select coordinator</option>
                      {(coordinatorsQuery.data ?? []).map((c) => (
                        <option key={c.id} value={c.id}>{c.fullName}</option>
                      ))}
                    </select>
                    <button
                      onClick={() => handleAssignCoordinator(row.id)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-700"
                    >
                      Confirm
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => setAssigningClassId(row.id)}
                    className="text-xs font-medium text-gray-500 hover:text-primary-600"
                  >
                    Assign coordinator
                  </button>
                )
            }
          ]}
          rows={(classesQuery.data ?? []).map((c) => ({ ...c, id: c.id }))}
          emptyMessage="No classes exist for the current academic year yet."
        />
      </Card>
    </div>
  );
}
