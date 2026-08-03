import { useState } from 'react';
import { useUsers, useCreateUser, useSetUserActive } from '../../api/userApi';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';
import Button from '../../shared/ui/Button';

export default function ClassCoordinatorsPage() {
  const coordinatorsQuery = useUsers('CLASS_COORDINATOR');
  const createUserMutation = useCreateUser();
  const setActiveMutation = useSetUserActive();

  const [form, setForm] = useState({ username: '', password: '', fullName: '' });
  const [error, setError] = useState('');

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    try {
      await createUserMutation.mutateAsync({ ...form, role: 'CLASS_COORDINATOR' });
      setForm({ username: '', password: '', fullName: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create coordinator account.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Class Coordinators</h1>
        <p className="text-sm text-gray-500 mt-1">
          Create Class Coordinator accounts. Assign them to a class under Classes.
        </p>
      </div>

      <Card title="Add Coordinator">
        <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-3 gap-3 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Full Name</label>
            <input
              type="text"
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Username</label>
            <input
              type="text"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Password</label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
              minLength={6}
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div className="md:col-span-3">
            <Button type="submit" loading={createUserMutation.isPending}>Add Coordinator</Button>
          </div>
        </form>
        {error && <div className="text-sm text-red-600 mt-3">{error}</div>}
      </Card>

      <Card title="All Coordinators">
        <Table
          columns={[
            { key: 'fullName', header: 'Name' },
            { key: 'username', header: 'Username' },
            {
              key: 'active',
              header: 'Status',
              render: (row) => (
                <button
                  onClick={() => setActiveMutation.mutate({ id: row.id, active: !row.active })}
                  className={
                    row.active
                      ? 'text-xs font-medium text-green-700 bg-green-50 px-2.5 py-1 rounded-full hover:bg-green-100'
                      : 'text-xs font-medium text-gray-500 bg-gray-100 px-2.5 py-1 rounded-full hover:bg-gray-200'
                  }
                >
                  {row.active ? 'Active' : 'Inactive'}
                </button>
              )
            }
          ]}
          rows={(coordinatorsQuery.data ?? []).map((u) => ({ ...u, id: u.id }))}
          emptyMessage="No coordinator accounts yet."
        />
      </Card>
    </div>
  );
}