export const THEORY_SLOTS = [
  { slot: 1, label: '8:30–9:30 AM' },
  { slot: 2, label: '9:30–10:30 AM' },
  { slot: 3, label: '10:30–11:30 AM' },
  { slot: 4, label: '11:30 AM–12:30 PM' },
  { slot: 5, label: '1:15–2:15 PM' },
  { slot: 6, label: '2:15–3:15 PM' },
  { slot: 7, label: '3:15–4:15 PM' }
];

export const PRACTICAL_SLOTS = [
  { slot: 1, label: '8:30–10:30 AM' },
  { slot: 2, label: '10:30 AM–12:30 PM' },
  { slot: 3, label: '1:15–3:15 PM' },
  { slot: 4, label: '3:15–5:15 PM' }
];

export function slotsForType(subjectType) {
  return subjectType === 'PR' ? PRACTICAL_SLOTS : THEORY_SLOTS;
}