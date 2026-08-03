import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

/**
 * Shared across Teacher and Coordinator Reports pages. Expects data as
 * [{ date: 'YYYY-MM-DD', percentage: number }], sorted ascending by date.
 */
export default function AttendanceTrendChart({ data }) {
  if (!data || data.length === 0) {
    return <div className="text-sm text-gray-400 text-center py-8">No trend data available for this range.</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
        <XAxis dataKey="date" tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} stroke="#9ca3af" />
        <Tooltip />
        <Line type="monotone" dataKey="percentage" stroke="#2563eb" strokeWidth={2} dot={{ r: 3 }} />
      </LineChart>
    </ResponsiveContainer>
  );
}