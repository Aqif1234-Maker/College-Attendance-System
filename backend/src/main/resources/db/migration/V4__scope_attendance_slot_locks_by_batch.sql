SET @scope_key_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'attendance_session_slot_locks'
      AND COLUMN_NAME = 'scope_key'
);

SET @sql := IF(
    @scope_key_exists = 0,
    'ALTER TABLE attendance_session_slot_locks ADD COLUMN scope_key BIGINT NULL AFTER period_unit',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE attendance_session_slot_locks lock_row
JOIN attendance_sessions session_row ON session_row.id = lock_row.session_id
JOIN batches batch_row ON batch_row.id = session_row.batch_id
SET lock_row.scope_key = CASE
    WHEN batch_row.is_whole_class = TRUE THEN 0
    ELSE batch_row.id
END
WHERE lock_row.scope_key IS NULL;

ALTER TABLE attendance_session_slot_locks
    MODIFY COLUMN scope_key BIGINT NOT NULL;

SET @class_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'attendance_session_slot_locks'
      AND INDEX_NAME = 'ix_assl_class_id'
);

SET @sql := IF(
    @class_index_exists = 0,
    'CREATE INDEX ix_assl_class_id ON attendance_session_slot_locks (class_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE attendance_session_slot_locks
    DROP INDEX ux_assl,
    ADD CONSTRAINT ux_assl UNIQUE (class_id, session_date, period_unit, scope_key);
