import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const STATUS_COLORS = {
  EXCELLENT: '#2563eb',
  GOOD: '#16a34a',
  WARNING: '#d97706',
  DEFAULTER: '#dc2626'
};

/**
 * Shared across Teacher and Coordinator Reports pages (spec §8) — takes the raw
 * ReportRowDto[] and derives the distribution itself, so callers never duplicate
 * this aggregation logic.
 */
export default function AttendanceDistributionChart({ rows }) {
  const counts = rows.reduce((acc, row) => {
    acc[row.status] = (acc[row.status] ?? 0) + 1;
    return acc;
  }, {});

  const data = Object.entries(counts).map(([status, count]) => ({
    name: status.charAt(0) + status.slice(1).toLowerCase(),
    value: count,
    status
  }));

  if (data.length === 0) {
    return <div className="text-sm text-gray-400 text-center py-8">No data to chart yet.</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <PieChart>
        <Pie data={data} dataKey="value" nameKey="name" innerRadius={60} outerRadius={90} paddingAngle={2}>
          {data.map((entry) => (
            <Cell key={entry.status} fill={STATUS_COLORS[entry.status] ?? '#9ca3af'} />
          ))}
        </Pie>
        <Tooltip />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
}