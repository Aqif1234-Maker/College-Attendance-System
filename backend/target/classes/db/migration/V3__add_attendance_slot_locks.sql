CREATE TABLE attendance_session_slot_locks (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id   BIGINT NOT NULL,
    class_id     BIGINT NOT NULL,
    session_date DATE NOT NULL,
    period_unit  TINYINT NOT NULL,
    CONSTRAINT fk_assl_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_assl_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT ux_assl UNIQUE (class_id, session_date, period_unit)
) ENGINE=InnoDB;
