import { NavLink } from 'react-router-dom';

/**
 * Renders whatever nav item list it's given — the role-specific item lists
 * (per spec §2.1–§2.3) live in each Layout, not here. This stays a dumb
 * presentational list so it's reused identically by all three layouts.
 */
export default function Sidebar({ items, title }) {
  return (
    <aside className="w-60 min-h-screen bg-white border-r border-gray-100 flex flex-col">
      <div className="px-5 py-5 border-b border-gray-100">
        <span className="text-lg font-semibold text-gray-900">{title}</span>
      </div>
      <nav className="flex-1 px-3 py-4 space-y-1">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              isActive
                ? 'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium bg-primary-50 text-primary-700'
                : 'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-50'
            }
          >
            {item.icon}
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}