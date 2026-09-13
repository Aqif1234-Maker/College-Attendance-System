import { Outlet } from 'react-router-dom';
import {
  LayoutDashboard, School, BookOpen, UserCog, Users2, Users, CheckSquare, History, BarChart3, KeyRound
} from 'lucide-react';
import Sidebar from '../ui/Sidebar';
import { useAuth } from '../../hooks/useAuth';

const COORDINATOR_NAV = [
  { to: '/coordinator', label: 'Dashboard', icon: <LayoutDashboard size={18} /> },
  { to: '/coordinator/classes', label: 'Assigned Classes', icon: <School size={18} /> },
  { to: '/coordinator/subjects', label: 'Subjects', icon: <BookOpen size={18} /> },
  { to: '/coordinator/faculty-assignment', label: 'Faculty Assignment', icon: <UserCog size={18} /> },
  { to: '/coordinator/batches', label: 'Batch Management', icon: <Users2 size={18} /> },
  { to: '/coordinator/students', label: 'Students', icon: <Users size={18} /> },
  { to: '/coordinator/oe-access', label: 'OE Access', icon: <KeyRound size={18} /> },
  { to: '/coordinator/attendance', label: 'Take Attendance', icon: <CheckSquare size={18} /> },
  { to: '/coordinator/attendance-history', label: 'Attendance History', icon: <History size={18} /> },
  { to: '/coordinator/reports', label: 'Reports', icon: <BarChart3 size={18} /> }
];

export default function CoordinatorLayout() {
  const { fullName, logout } = useAuth();

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar items={COORDINATOR_NAV} title="Coordinator" />
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