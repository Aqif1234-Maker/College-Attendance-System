import { useCurrentAcademicYear } from '../../api/academicYearApi';
import Card from '../../shared/ui/Card';

export default function SettingsPage() {
  const currentYearQuery = useCurrentAcademicYear();

  return (
    <div className="space-y-5 max-w-lg">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Settings</h1>
        <p className="text-sm text-gray-500 mt-1">System configuration.</p>
      </div>

      <Card title="Current State">
        <div className="space-y-3 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-500">Active Academic Year</span>
            <span className="font-medium text-gray-900">
              {currentYearQuery.data?.label ?? 'None set'}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">Attendance status thresholds</span>
            <span className="font-medium text-gray-900">85% / 75% / 65%</span>
          </div>
        </div>
      </Card>

      <div className="text-xs text-gray-400 bg-gray-50 border border-gray-100 rounded-xl px-4 py-3">
        Configurable settings (e.g. adjustable status thresholds) are not yet backed by a
        settings API on the backend — this page currently shows read-only system state.
      </div>
    </div>
  );
}