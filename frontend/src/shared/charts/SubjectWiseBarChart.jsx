import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

/**
 * Shared across Teacher and Coordinator Reports pages. Groups the raw
 * ReportRowDto[] by subjectName and averages percentage per subject —
 * useful when a report spans multiple subjects (e.g. Coordinator view).
 */
export default function SubjectWiseBarChart({ rows }) {
  const bySubject = rows.reduce((acc, row) => {
    if (!acc[row.subjectName]) {
      acc[row.subjectName] = { total: 0, count: 0 };
    }
    acc[row.subjectName].total += row.percentage;
    acc[row.subjectName].count += 1;
    return acc;
  }, {});

  const data = Object.entries(bySubject).map(([subjectName, { total, count }]) => ({
    subjectName,
    averagePercentage: Math.round((total / count) * 10) / 10
  }));

  if (data.length === 0) {
    return <div className="text-sm text-gray-400 text-center py-8">No data to chart yet.</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={data}>
        <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
        <XAxis dataKey="subjectName" tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <Tooltip />
        <Bar dataKey="averagePercentage" fill="#2563eb" radius={[6, 6, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}