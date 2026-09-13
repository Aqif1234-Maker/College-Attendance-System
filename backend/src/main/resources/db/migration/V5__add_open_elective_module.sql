-- ============================================================
-- V5__add_open_elective_module.sql
-- ============================================================

-- ---- REQUIRED CHANGE TO AN EXISTING TABLE ----
-- Widens subjects.type so an OE subject is a real row in the existing
-- subjects table, letting it plug into attendance_sessions, batches,
-- attendance_session_slot_locks, and reports with zero new code paths
-- for anything except student-roster resolution.
ALTER TABLE subjects
    MODIFY COLUMN type ENUM('TH','PR','OE') NOT NULL;

-- ============ SEMESTERS ============
-- Lifecycle unit for OE setup, separate from academic_years since OE setup
-- opens/closes per semester while the academic year stays constant.
-- class_a_id/class_b_id are the two divisions this semester's OE spans
-- (e.g. TE-A, TE-B) — the single source of truth for which class a
-- Combined/Separate-mode OE subject anchors to (see OeSubjectService).
CREATE TABLE semesters (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    academic_year_id  BIGINT NOT NULL,
    label             VARCHAR(30) NOT NULL,
    status            ENUM('ACTIVE','CLOSED') NOT NULL DEFAULT 'ACTIVE',
    class_a_id        BIGINT NOT NULL,
    class_b_id        BIGINT NOT NULL,
    closed_at         TIMESTAMP NULL,
    closed_by         BIGINT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sem_year    FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_sem_classA  FOREIGN KEY (class_a_id) REFERENCES classes(id),
    CONSTRAINT fk_sem_classB  FOREIGN KEY (class_b_id) REFERENCES classes(id),
    CONSTRAINT fk_sem_closer  FOREIGN KEY (closed_by) REFERENCES users(id),
    CONSTRAINT ux_semester_year_label UNIQUE (academic_year_id, label),
    CONSTRAINT ck_sem_distinct_classes CHECK (class_a_id <> class_b_id)
) ENGINE=InnoDB;

-- ============ OE TEACHING SLOTS ============
-- Phase 1: the coordinator's reservation (mode + teacher), locked BEFORE
-- any named subject exists. The DB-atomic lock the poster describes —
-- only one teacher can hold a given mode per semester.
CREATE TABLE oe_teaching_slots (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    semester_id  BIGINT NOT NULL,
    mode         ENUM('COMBINED','SEPARATE_A','SEPARATE_B') NOT NULL,
    teacher_id   BIGINT NOT NULL,
    assigned_by  BIGINT NOT NULL,
    assigned_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ots_semester FOREIGN KEY (semester_id) REFERENCES semesters(id),
    CONSTRAINT fk_ots_teacher  FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT fk_ots_assigner FOREIGN KEY (assigned_by) REFERENCES users(id),
    CONSTRAINT ux_ots_mode_per_semester UNIQUE (semester_id, mode)
) ENGINE=InnoDB;

-- ============ OE SUBJECTS ============
-- Phase 2: created by the teacher once they hold a slot. This is the ONE
-- authoritative link between an OE reservation and the real subjects row
-- the existing attendance system already knows how to use.
CREATE TABLE oe_subjects (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id          BIGINT NOT NULL UNIQUE,
    oe_teaching_slot_id BIGINT NOT NULL UNIQUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_oesub_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_oesub_slot    FOREIGN KEY (oe_teaching_slot_id) REFERENCES oe_teaching_slots(id)
) ENGINE=InnoDB;

-- ============ OE ENROLLMENTS ============
-- Students are picked from the EXISTING students table only — this table
-- never creates a new student, only links existing ones to an OE subject.
CREATE TABLE oe_enrollments (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    oe_subject_id BIGINT NOT NULL,
    student_id    BIGINT NOT NULL,
    enrolled_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_oeenr_subject FOREIGN KEY (oe_subject_id) REFERENCES oe_subjects(id),
    CONSTRAINT fk_oeenr_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT ux_oeenr UNIQUE (oe_subject_id, student_id)
) ENGINE=InnoDB;

-- ============ AUDIT LOGS ============
-- Generic and reusable beyond OE — every mutating OE action writes here
-- (slot assigned/reassigned, subject created, semester closed, etc.).
CREATE TABLE audit_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id     BIGINT NOT NULL,
    action       VARCHAR(60) NOT NULL,
    entity_type  VARCHAR(60) NOT NULL,
    entity_id    BIGINT NULL,
    before_value JSON NULL,
    after_value  JSON NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX ix_audit_entity ON audit_logs (entity_type, entity_id);