/**
 * Generic sticky-header, zebra-striped table (spec §4.4, §5.3).
 * columns: [{ key, header, render?(row) }]
 */
export default function Table({ columns, rows, emptyMessage = 'No data to display' }) {
  return (
    <div className="rounded-xl border border-gray-100 overflow-auto max-h-[520px]">
      <table className="w-full text-sm">
        <thead className="sticky top-0 bg-gray-50 z-10">
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className="text-left font-medium text-gray-500 px-4 py-3 border-b border-gray-100 whitespace-nowrap"
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="text-center text-gray-400 py-8">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            rows.map((row, i) => (
              <tr
                key={row.id ?? i}
                className={i % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50/50 hover:bg-gray-50'}
              >
                {columns.map((col) => (
                  <td key={col.key} className="px-4 py-3 border-b border-gray-50 text-gray-700 whitespace-nowrap">
                    {col.render ? col.render(row) : row[col.key]}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}