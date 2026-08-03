import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../features/auth/LoginPage';
import ProtectedRoute from './ProtectedRoute';
import RoleGate from './RoleGate';

import AdminLayout from '../shared/layouts/AdminLayout';
import CoordinatorLayout from '../shared/layouts/CoordinatorLayout';
import TeacherLayout from '../shared/layouts/TeacherLayout';

import AdminDashboard from '../features/admin/AdminDashboard';
import FacultyPage from '../features/admin/FacultyPage';
import ClassCoordinatorsPage from '../features/admin/ClassCoordinatorsPage';
import AcademicYearPage from '../features/admin/AcademicYearPage';
import ClassesPage from '../features/admin/ClassesPage';
import UsersPage from '../features/admin/UsersPage';
import SettingsPage from '../features/admin/SettingsPage';

import CoordinatorDashboard from '../features/coordinator/CoordinatorDashboard';
import AssignedClassesPage from '../features/coordinator/AssignedClassesPage';
import SubjectsPage from '../features/coordinator/SubjectsPage';
import FacultyAssignmentPage from '../features/coordinator/FacultyAssignmentPage';
import StudentsPage from '../features/coordinator/StudentsPage';
import CoordinatorReportsPage from '../features/coordinator/CoordinatorReportsPage';

import TeacherDashboard from '../features/teacher/TeacherDashboard';
import ProfilePage from '../features/teacher/ProfilePage';
import TakeAttendancePage from '../features/teacher/attendance/TakeAttendancePage';
import AttendanceHistoryPage from '../features/teacher/attendance/AttendanceHistoryPage';
import TeacherReportsPage from '../features/teacher/reports/TeacherReportsPage';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        {/* --- Admin --- */}
        <Route element={<RoleGate allowedRoles={['ADMIN']} />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/faculty" element={<FacultyPage />} />
            <Route path="/admin/coordinators" element={<ClassCoordinatorsPage />} />
            <Route path="/admin/academic-years" element={<AcademicYearPage />} />
            <Route path="/admin/classes" element={<ClassesPage />} />
            <Route path="/admin/users" element={<UsersPage />} />
            <Route path="/admin/settings" element={<SettingsPage />} />
          </Route>
        </Route>

        {/* --- Coordinator (includes Take Attendance / Attendance History / Reports,
             per the union sidebar decision — a Coordinator is a Teacher plus extras) --- */}
        <Route element={<RoleGate allowedRoles={['CLASS_COORDINATOR']} />}>
          <Route element={<CoordinatorLayout />}>
            <Route path="/coordinator" element={<CoordinatorDashboard />} />
            <Route path="/coordinator/classes" element={<AssignedClassesPage />} />
            <Route path="/coordinator/subjects" element={<SubjectsPage />} />
            <Route path="/coordinator/faculty-assignment" element={<FacultyAssignmentPage />} />
            <Route path="/coordinator/batches" element={<Navigate to="/coordinator/subjects" replace />} />
            <Route path="/coordinator/students" element={<StudentsPage />} />
            <Route path="/coordinator/attendance" element={<TakeAttendancePage />} />
            <Route path="/coordinator/attendance-history" element={<AttendanceHistoryPage />} />
            <Route path="/coordinator/reports" element={<CoordinatorReportsPage />} />
          </Route>
        </Route>

        {/* --- Teacher --- */}
        <Route element={<RoleGate allowedRoles={['TEACHER']} allowTeacherRoutes />}>
          <Route element={<TeacherLayout />}>
            <Route path="/teacher" element={<TeacherDashboard />} />
            <Route path="/teacher/attendance" element={<TakeAttendancePage />} />
            <Route path="/teacher/attendance-history" element={<AttendanceHistoryPage />} />
            <Route path="/teacher/reports" element={<TeacherReportsPage />} />
            <Route path="/teacher/profile" element={<ProfilePage />} />
          </Route>
        </Route>
      </Route>

      <Route
        path="/unauthorized"
        element={<div className="p-8 text-gray-600">You are not authorized to view this page.</div>}
      />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
