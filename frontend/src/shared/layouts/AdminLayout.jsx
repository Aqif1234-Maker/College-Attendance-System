import { Outlet } from 'react-router-dom';
import { LayoutDashboard, Users, UserCog, CalendarRange, School, ShieldCheck, Settings } from 'lucide-react';
import Sidebar from '../ui/Sidebar';
import { useAuth } from '../../hooks/useAuth';

const ADMIN_NAV = [
  { to: '/admin', label: 'Dashboard', icon: <LayoutDashboard size={18} /> },
  { to: '/admin/faculty', label: 'Faculty', icon: <Users size={18} /> },
  { to: '/admin/coordinators', label: 'Class Coordinators', icon: <UserCog size={18} /> },
  { to: '/admin/academic-years', label: 'Academic Year', icon: <CalendarRange size={18} /> },
  { to: '/admin/classes', label: 'Classes', icon: <School size={18} /> },
  { to: '/admin/users', label: 'Users', icon: <ShieldCheck size={18} /> },
  { to: '/admin/settings', label: 'Settings', icon: <Settings size={18} /> }
];

export default function AdminLayout() {
  const { fullName, logout } = useAuth();

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar items={ADMIN_NAV} title="Admin" />
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