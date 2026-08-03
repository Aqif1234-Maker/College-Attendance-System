import { Outlet } from 'react-router-dom';
import { LayoutDashboard, CheckSquare, History, BarChart3, User } from 'lucide-react';
import Sidebar from '../ui/Sidebar';
import { useAuth } from '../../hooks/useAuth';

const TEACHER_NAV = [
  { to: '/teacher', label: 'Dashboard', icon: <LayoutDashboard size={18} /> },
  { to: '/teacher/attendance', label: 'Take Attendance', icon: <CheckSquare size={18} /> },
  { to: '/teacher/attendance-history', label: 'Attendance History', icon: <History size={18} /> },
  { to: '/teacher/reports', label: 'Reports', icon: <BarChart3 size={18} /> },
  { to: '/teacher/profile', label: 'Profile', icon: <User size={18} /> }
];

export default function TeacherLayout() {
  const { fullName, logout } = useAuth();

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar items={TEACHER_NAV} title="Teacher" />
      <div className="flex-1 flex flex-col">
        <header className="h-16 bg-white border-b border-gray-100 flex items-center justify-end px-6 gap-4">
          <span className="text-sm text-gray-600">{fullName}</span>
          <button onClick={logout} className="text-sm text-primary-600 hover:text-primary-700 font-medium">
            Sign out
          </button>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}