import { Link } from 'react-router-dom';
import { CheckSquare, BarChart3 } from 'lucide-react';
import { useMyClasses } from '../../api/assignmentApi';
import { useAttendanceHistory } from '../../api/attendanceApi';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../shared/ui/Card';

export default function TeacherDashboard() {
  const { fullName } = useAuth();
  const classesQuery = useMyClasses();
  const historyQuery = useAttendanceHistory();

  const recentSessions = (historyQuery.data ?? []).slice(0, 5);

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Welcome, {fullName}</h1>
        <p className="text-sm text-gray-500 mt-1">Here's what's assigned to you.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Link to="/teacher/attendance">
          <Card className="hover:border-primary-200 transition-colors cursor-pointer">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center">
                <CheckSquare className="text-primary-600" size={20} />
              </div>
              <div>
                <div className="text-sm font-semibold text-gray-900">Take Attendance</div>
                <div className="text-xs text-gray-500">Start a new session</div>
              </div>
            </div>
          </Card>
        </Link>

        <Link to="/teacher/reports">
          <Card className="hover:border-primary-200 transition-colors cursor-pointer">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center">
                <BarChart3 className="text-primary-600" size={20} />
              </div>
              <div>
                <div className="text-sm font-semibold text-gray-900">View Reports</div>
                <div className="text-xs text-gray-500">Attendance percentage & analytics</div>
              </div>
            </div>
          </Card>
        </Link>
      </div>

      <Card title="Your Assigned Classes">
        {classesQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : (classesQuery.data ?? []).length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">
            No classes assigned yet. Contact your Coordinator.
          </div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {classesQuery.data.map((c) => (
              <span key={c.id} className="px-3 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-700">
                {c.name}
              </span>
            ))}
          </div>
        )}
      </Card>

      <Card title="Recent Sessions">
        {historyQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : recentSessions.length === 0 ? (
          <div className="text-sm text-gray-400 py-4 text-center">No sessions yet.</div>
        ) : (
          <div className="divide-y divide-gray-50">
            {recentSessions.map((s) => (
              <div key={s.id} className="flex items-center justify-between py-2.5 text-sm">
                <span className="text-gray-700">
                  {s.className} · {s.subjectName} · {s.batchLabel}
                </span>
                <span className="text-gray-400">{s.sessionDate}</span>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}