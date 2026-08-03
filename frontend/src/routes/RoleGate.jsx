import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Gates a route subtree to a set of allowed roles. A Coordinator is treated as a superset
 * of Teacher (spec §2.2) — pass allowTeacherRoutes to also admit CLASS_COORDINATOR on
 * Teacher-scoped routes (Take Attendance, Reports) without duplicating those pages.
 */
export default function RoleGate({ allowedRoles, allowTeacherRoutes = false }) {
  const { role } = useAuth();

  const effectiveAllowed = allowTeacherRoutes
    ? [...allowedRoles, 'CLASS_COORDINATOR']
    : allowedRoles;

  if (!effectiveAllowed.includes(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}