import { useUsers } from '../../api/userApi';
import { useClasses } from '../../api/classApi';
import Card from '../../shared/ui/Card';

export default function AdminDashboard() {
  const usersQuery = useUsers();
  const classesQuery = useClasses();

  const users = usersQuery.data ?? [];
  const teachingStaffCount = users.filter((u) => u.role === 'TEACHER' || u.role === 'CLASS_COORDINATOR').length;
  const coordinatorCount = users.filter((u) => u.role === 'CLASS_COORDINATOR').length;
  const activeCount = users.filter((u) => u.active).length;

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Admin Dashboard</h1>
        <p className="text-sm text-gray-500 mt-1">System overview.</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <SummaryCard label="Total Users" value={users.length} loading={usersQuery.isLoading} />
        <SummaryCard label="Active Users" value={activeCount} loading={usersQuery.isLoading} />
        <SummaryCard label="Teaching Staff" value={teachingStaffCount} loading={usersQuery.isLoading} />
        <SummaryCard label="Coordinators" value={coordinatorCount} loading={usersQuery.isLoading} />
      </div>

      <Card title="Classes">
        {classesQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : (classesQuery.data ?? []).length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">No classes created yet.</div>
        ) : (
          <div className="divide-y divide-gray-50">
            {classesQuery.data.map((c) => (
              <div key={c.id} className="flex items-center justify-between py-2.5 text-sm">
                <span className="font-medium text-gray-800">{c.name}</span>
                <span className="text-gray-500">
                  {c.coordinatorName ? `Coordinator: ${c.coordinatorName}` : 'No coordinator assigned'}
                </span>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

function SummaryCard({ label, value, loading }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-4">
      <div className="text-xs font-medium text-gray-500">{label}</div>
      <div className="text-xl font-semibold text-gray-900 mt-1">{loading ? '—' : value}</div>
    </div>
  );
}
