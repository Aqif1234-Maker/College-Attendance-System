package com.attendance.attendance;

import com.attendance.subject.SubjectType;

import java.util.List;

public final class SlotSchedule {

    private static final List<SlotWindow> THEORY_SLOTS = List.of(
            new SlotWindow(1, "8:30–9:30 AM", List.of(1)),
            new SlotWindow(2, "9:30–10:30 AM", List.of(2)),
            new SlotWindow(3, "10:30–11:30 AM", List.of(3)),
            new SlotWindow(4, "11:30 AM–12:30 PM", List.of(4)),
            new SlotWindow(5, "1:15–2:15 PM", List.of(5)),
            new SlotWindow(6, "2:15–3:15 PM", List.of(6)),
            new SlotWindow(7, "3:15–4:15 PM", List.of(7))
    );

    private static final List<SlotWindow> PRACTICAL_SLOTS = List.of(
            new SlotWindow(1, "8:30–10:30 AM", List.of(1, 2)),
            new SlotWindow(2, "10:30 AM–12:30 PM", List.of(3, 4)),
            new SlotWindow(3, "1:15–3:15 PM", List.of(5, 6)),
            new SlotWindow(4, "3:15–5:15 PM", List.of(7, 8))
    );

    private SlotSchedule() {
    }

    public static SlotWindow forTypeAndSlot(SubjectType type, int slot) {
        return slotsForType(type).stream()
                .filter(window -> window.slot() == slot)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown slot " + slot + " for subject type " + type));
    }

    private static List<SlotWindow> slotsForType(SubjectType type) {
        return type == SubjectType.PR ? PRACTICAL_SLOTS : THEORY_SLOTS;
    }

    public record SlotWindow(
            int slot,
            String label,
            List<Integer> periodUnits
    ) {
        public boolean overlaps(SlotWindow other) {
            return periodUnits.stream().anyMatch(other.periodUnits::contains);
        }
    }
}
