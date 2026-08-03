export default function Card({ children, title, action, className = '' }) {
  return (
    <div className={`bg-white rounded-2xl border border-gray-100 p-5 ${className}`}>
      {(title || action) && (
        <div className="flex items-center justify-between mb-4">
          {title && <h2 className="text-base font-semibold text-gray-900">{title}</h2>}
          {action}
        </div>
      )}
      {children}
    </div>
  );
}