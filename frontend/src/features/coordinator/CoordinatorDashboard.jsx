import { Link } from 'react-router-dom';
import { School, BookOpen, Users, CheckSquare, BarChart3 } from 'lucide-react';
import { useMyClasses } from '../../api/assignmentApi';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../shared/ui/Card';

const QUICK_LINKS = [
  { to: '/coordinator/classes', label: 'Assigned Classes', icon: School },
  { to: '/coordinator/subjects', label: 'Subjects', icon: BookOpen },
  { to: '/coordinator/students', label: 'Students', icon: Users },
  { to: '/coordinator/attendance', label: 'Take Attendance', icon: CheckSquare },
  { to: '/coordinator/reports', label: 'Reports', icon: BarChart3 }
];

export default function CoordinatorDashboard() {
  const { fullName } = useAuth();
  // Coordinator's own teacher-side assignments (self-assignment, spec §2.2 union),
  // not the classes they coordinate — that list comes from AssignedClassesPage.
  const myAssignedClassesQuery = useMyClasses();

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Welcome, {fullName}</h1>
        <p className="text-sm text-gray-500 mt-1">Manage your classes, subjects, and faculty assignments.</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
        {QUICK_LINKS.map(({ to, label, icon: Icon }) => (
          <Link key={to} to={to}>
            <Card className="hover:border-primary-200 transition-colors cursor-pointer">
              <div className="flex flex-col items-center text-center gap-2 py-2">
                <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center">
                  <Icon className="text-primary-600" size={20} />
                </div>
                <span className="text-xs font-medium text-gray-700">{label}</span>
              </div>
            </Card>
          </Link>
        ))}
      </div>

      <Card title="Your Own Teaching Assignments">
        {myAssignedClassesQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : (myAssignedClassesQuery.data ?? []).length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">
            You have no self-assigned teaching duties yet. Assign yourself under Faculty Assignment.
          </div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {myAssignedClassesQuery.data.map((c) => (
              <span key={c.id} className="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-700">
                {c.name}
              </span>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}