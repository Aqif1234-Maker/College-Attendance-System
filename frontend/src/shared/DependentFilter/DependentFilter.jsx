import { ChevronDown } from 'lucide-react';

function Select({ label, value, onChange, options, disabled, placeholder }) {
  return (
    <div className="flex-1 min-w-[140px]">
      <label className="block text-xs font-medium text-gray-500 mb-1">{label}</label>
      <div className="relative">
        <select
          value={value ?? ''}
          onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
          disabled={disabled}
          className="w-full appearance-none rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm text-gray-800 disabled:bg-gray-50 disabled:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="">{placeholder}</option>
          {options.map((opt) => (
            <option key={opt.id} value={opt.id}>
              {opt.name ?? opt.label}
            </option>
          ))}
        </select>
        <ChevronDown className="absolute right-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
      </div>
    </div>
  );
}

/**
 * The one shared Academic Year → Class → Subject → Batch filter (spec §3, §8).
 * Academic Year is read-only/auto-selected — never a manual dropdown.
 * Pass the `filter` object returned by useDependentFilter().
 */
export default function DependentFilter({ filter }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-4">
      <div className="mb-3">
        <span className="text-xs font-medium text-gray-500">Academic Year</span>
        <div className="text-sm font-semibold text-gray-800">
          {filter.academicYearLoading ? 'Loading…' : filter.academicYear?.label ?? 'No current academic year set'}
        </div>
      </div>

      <div className="flex flex-wrap gap-3">
        <Select
          label="Class"
          value={filter.classId}
          onChange={filter.selectClass}
          options={filter.classes}
          disabled={filter.classesLoading}
          placeholder={filter.classesLoading ? 'Loading…' : 'Select class'}
        />
        <Select
          label="Subject"
          value={filter.subjectId}
          onChange={filter.selectSubject}
          options={filter.subjects}
          disabled={!filter.classId || filter.subjectsLoading}
          placeholder={!filter.classId ? 'Select class first' : 'Select subject'}
        />
        <Select
          label="Batch"
          value={filter.batchId}
          onChange={filter.selectBatch}
          options={filter.batches}
          disabled={!filter.subjectId || filter.batchesLoading}
          placeholder={!filter.subjectId ? 'Select subject first' : 'Select batch'}
        />
      </div>
    </div>
  );
}