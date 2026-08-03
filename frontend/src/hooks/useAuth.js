import { useAuthStore } from '../store/authStore';

/**
 * Cross-cutting auth hook — current user identity, role, and logout action.
 * Not tied to a single backend resource, so it lives here rather than in api/.
 */
export function useAuth() {
  const token = useAuthStore((state) => state.token);
  const userId = useAuthStore((state) => state.userId);
  const username = useAuthStore((state) => state.username);
  const fullName = useAuthStore((state) => state.fullName);
  const role = useAuthStore((state) => state.role);
  const logout = useAuthStore((state) => state.logout);

  return {
    isLoggedIn: Boolean(token),
    userId,
    username,
    fullName,
    role,
    isAdmin: role === 'ADMIN',
    isCoordinator: role === 'CLASS_COORDINATOR',
    isTeacher: role === 'TEACHER',
    // A Coordinator is a Teacher with extra responsibilities (spec §2.2) —
    // anywhere "can this user take attendance" is asked, check this, not isTeacher alone.
    canActAsTeacher: role === 'TEACHER' || role === 'CLASS_COORDINATOR',
    logout
  };
}