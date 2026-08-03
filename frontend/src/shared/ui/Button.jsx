const VARIANTS = {
  primary: 'bg-primary-600 hover:bg-primary-700 text-white',
  secondary: 'bg-gray-100 hover:bg-gray-200 text-gray-700',
  danger: 'bg-red-50 hover:bg-red-100 text-red-600',
  ghost: 'bg-transparent hover:bg-gray-50 text-gray-600'
};

export default function Button({
  children,
  variant = 'primary',
  disabled = false,
  loading = false,
  type = 'button',
  onClick,
  className = ''
}) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={`px-4 py-2.5 rounded-xl text-sm font-medium transition-colors disabled:opacity-60 disabled:cursor-not-allowed ${VARIANTS[variant]} ${className}`}
    >
      {loading ? 'Please wait…' : children}
    </button>
  );
}