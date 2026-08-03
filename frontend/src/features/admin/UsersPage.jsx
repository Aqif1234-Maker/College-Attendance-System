import { useState } from 'react';
import { useUsers, useCreateUser, useSetUserActive, useResetUserPassword } from '../../api/userApi';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';
import Button from '../../shared/ui/Button';

const ROLE_OPTIONS = [
  { value: 'TEACHER', label: 'Teacher' },
  { value: 'CLASS_COORDINATOR', label: 'Class Coordinator' },
  { value: 'ADMIN', label: 'Admin' }
];

export default function UsersPage() {
  const usersQuery = useUsers();
  const createUserMutation = useCreateUser();
  const setActiveMutation = useSetUserActive();
  const resetPasswordMutation = useResetUserPassword();

  const [form, setForm] = useState({ username: '', password: '', fullName: '', role: 'TEACHER' });
  const [error, setError] = useState('');
  const [resettingId, setResettingId] = useState(null);
  const [newPassword, setNewPassword] = useState('');

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    try {
      await createUserMutation.mutateAsync(form);
      setForm({ username: '', password: '', fullName: '', role: 'TEACHER' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create user.');
    }
  }

  async function handleResetPassword(id) {
    if (!newPassword) return;
    try {
      await resetPasswordMutation.mutateAsync({ id, newPassword });
      setResettingId(null);
      setNewPassword('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reset password.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Users</h1>
        <p className="text-sm text-gray-500 mt-1">Create and manage all accounts, roles, and access.</p>
      </div>

      <Card title="Create Account">
        <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
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
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Role</label>
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              {ROLE_OPTIONS.map((r) => (
                <option key={r.value} value={r.value}>{r.label}</option>
              ))}
            </select>
          </div>
          <div className="md:col-span-4">
            <Button type="submit" loading={createUserMutation.isPending}>Create Account</Button>
          </div>
        </form>
        {error && <div className="text-sm text-red-600 mt-3">{error}</div>}
      </Card>

      <Card title="All Users">
        <Table
          columns={[
            { key: 'fullName', header: 'Name' },
            { key: 'username', header: 'Username' },
            { key: 'role', header: 'Role' },
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
            },
            {
              key: 'reset',
              header: '',
              render: (row) =>
                resettingId === row.id ? (
                  <div className="flex items-center gap-2">
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="New password"
                      className="rounded-lg border border-gray-200 px-2 py-1 text-xs w-32"
                    />
                    <button
                      onClick={() => handleResetPassword(row.id)}
                      className="text-xs font-medium text-primary-600 hover:text-primary-700"
                    >
                      Save
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => setResettingId(row.id)}
                    className="text-xs font-medium text-gray-500 hover:text-primary-600"
                  >
                    Reset Password
                  </button>
                )
            }
          ]}
          rows={(usersQuery.data ?? []).map((u) => ({ ...u, id: u.id }))}
          emptyMessage="No users yet."
        />
      </Card>
    </div>
  );
}