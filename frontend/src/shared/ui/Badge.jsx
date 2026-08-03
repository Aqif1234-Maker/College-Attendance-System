const STATUS_STYLES = {
  EXCELLENT: 'bg-blue-50 text-blue-700',
  GOOD: 'bg-green-50 text-green-700',
  WARNING: 'bg-amber-50 text-amber-700',
  DEFAULTER: 'bg-red-50 text-red-700'
};

const STATUS_DOT = {
  EXCELLENT: '🔵',
  GOOD: '🟢',
  WARNING: '🟠',
  DEFAULTER: '🔴'
};

/**
 * Renders a report-row status badge (spec §5.3). Also usable as a plain badge
 * for any short label by passing a raw `label` instead of `status`.
 */
export default function Badge({ status, label }) {
  if (status) {
    return (
      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${STATUS_STYLES[status] ?? 'bg-gray-50 text-gray-600'}`}>
        <span>{STATUS_DOT[status] ?? '⚪'}</span>
        {status.charAt(0) + status.slice(1).toLowerCase()}
      </span>
    );
  }

  return (
    <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-600">
      {label}
    </span>
  );
}