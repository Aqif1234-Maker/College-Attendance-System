INSERT IGNORE INTO attendance_session_slot_locks (session_id, class_id, session_date, period_unit, scope_key)
SELECT
    lock_row.session_id,
    CASE
        WHEN lock_row.class_id = semester_row.class_a_id THEN semester_row.class_b_id
        ELSE semester_row.class_a_id
    END AS paired_class_id,
    lock_row.session_date,
    lock_row.period_unit,
    lock_row.scope_key
FROM attendance_session_slot_locks lock_row
JOIN attendance_sessions session_row ON session_row.id = lock_row.session_id
JOIN oe_subjects oe_subject_row ON oe_subject_row.subject_id = session_row.subject_id
JOIN oe_teaching_slots slot_row ON slot_row.id = oe_subject_row.oe_teaching_slot_id
JOIN semesters semester_row ON semester_row.id = slot_row.semester_id
WHERE slot_row.mode = 'COMBINED'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_session_slot_locks paired_lock
      WHERE paired_lock.session_id = lock_row.session_id
        AND paired_lock.class_id = CASE
            WHEN lock_row.class_id = semester_row.class_a_id THEN semester_row.class_b_id
            ELSE semester_row.class_a_id
        END
        AND paired_lock.session_date = lock_row.session_date
        AND paired_lock.period_unit = lock_row.period_unit
        AND paired_lock.scope_key = lock_row.scope_key
  );
