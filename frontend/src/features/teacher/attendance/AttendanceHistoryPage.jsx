import { useState } from 'react';
import { useAttendanceHistory, useAttendanceSession } from '../../../api/attendanceApi';
import Card from '../../../shared/ui/Card';
import Table from '../../../shared/ui/Table';
import Badge from '../../../shared/ui/Badge';

const SLOT_COLUMNS = [
  { key: 'sessionDate', header: 'Date' },
  { key: 'className', header: 'Class' },
  { key: 'subjectName', header: 'Subject' },
  { key: 'batchLabel', header: 'Batch' },
  { key: 'slot', header: 'Slot', render: (row) => `Slot ${row.slot}` },
  {
    key: 'submitted',
    header: 'Status',
    render: (row) =>
      row.submitted ? (
        <Badge label="Submitted" />
      ) : (
        <span className="text-xs font-medium text-amber-600">Not submitted</span>
      )
  }
];

export default function AttendanceHistoryPage() {
  const historyQuery = useAttendanceHistory();
  const [expandedId, setExpandedId] = useState(null);

  const sessionDetailQuery = useAttendanceSession(expandedId);

  const rows = (historyQuery.data ?? []).map((session) => ({
    ...session,
    id: session.id
  }));

  const columnsWithAction = [
    ...SLOT_COLUMNS,
    {
      key: 'view',
      header: '',
      render: (row) => (
        <button
          onClick={() => setExpandedId(expandedId === row.id ? null : row.id)}
          className="text-xs font-medium text-primary-600 hover:text-primary-700"
        >
          {expandedId === row.id ? 'Hide' : 'View'}
        </button>
      )
    }
  ];

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Attendance History</h1>
        <p className="text-sm text-gray-500 mt-1">Sessions you've opened, most recent first.</p>
      </div>

      <Card>
        {historyQuery.isLoading ? (
          <div className="text-sm text-gray-400 py-8 text-center">Loading…</div>
        ) : (
          <Table
            columns={columnsWithAction}
            rows={rows}
            emptyMessage="No attendance sessions yet. Start one from Take Attendance."
          />
        )}
      </Card>

      {expandedId && (
        <Card title="Session Detail">
          {sessionDetailQuery.isLoading ? (
            <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
          ) : (
            <Table
              columns={[
                { key: 'rollNo', header: 'Roll No' },
                { key: 'studentName', header: 'Student Name' },
                {
                  key: 'status',
                  header: 'Status',
                  render: (row) =>
                    row.status ? (
                      <span
                        className={
                          row.status === 'PRESENT'
                            ? 'text-xs font-medium text-green-700'
                            : 'text-xs font-medium text-red-600'
                        }
                      >
                        {row.status === 'PRESENT' ? 'Present' : 'Absent'}
                      </span>
                    ) : (
                      <span className="text-xs text-gray-400">Not marked</span>
                    )
                }
              ]}
              rows={(sessionDetailQuery.data?.students ?? []).map((s) => ({ ...s, id: s.studentId }))}
            />
          )}
        </Card>
      )}
    </div>
  );
}