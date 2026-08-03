import { User, Shield } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../shared/ui/Card';

const ROLE_LABELS = {
  ADMIN: 'Administrator',
  CLASS_COORDINATOR: 'Class Coordinator',
  TEACHER: 'Teacher'
};

export default function ProfilePage() {
  const { fullName, username, role } = useAuth();

  return (
    <div className="space-y-5 max-w-lg">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Profile</h1>
        <p className="text-sm text-gray-500 mt-1">Your account details.</p>
      </div>

      <Card>
        <div className="flex items-center gap-4 mb-5">
          <div className="w-14 h-14 rounded-full bg-primary-50 flex items-center justify-center">
            <User className="text-primary-600" size={24} />
          </div>
          <div>
            <div className="text-base font-semibold text-gray-900">{fullName}</div>
            <div className="text-sm text-gray-500">@{username}</div>
          </div>
        </div>

        <div className="flex items-center gap-2 text-sm text-gray-600 border-t border-gray-50 pt-4">
          <Shield size={16} className="text-gray-400" />
          <span>Role:</span>
          <span className="font-medium text-gray-900">{ROLE_LABELS[role] ?? role}</span>
        </div>
      </Card>
    </div>
  );
}