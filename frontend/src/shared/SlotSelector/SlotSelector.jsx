import { slotsForType } from './slotDefinitions';

/**
 * The one shared slot-selection chip component (spec §4.2), reused identically for
 * Theory and Practical — only the slot set changes, based on subjectType.
 */
export default function SlotSelector({ subjectType, value, onChange }) {
  const slots = slotsForType(subjectType);

  return (
    <div className="flex flex-wrap gap-2">
      {slots.map((s) => {
        const selected = value === s.slot;
        return (
          <button
            key={s.slot}
            type="button"
            onClick={() => onChange(s.slot)}
            className={
              selected
                ? 'px-4 py-2 rounded-full text-sm font-medium bg-primary-600 text-white transition-colors'
                : 'px-4 py-2 rounded-full text-sm font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors'
            }
          >
            {s.label}
          </button>
        );
      })}
    </div>
  );
}