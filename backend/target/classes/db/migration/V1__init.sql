-- ===================== USERS & ROLES =====================
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    role            ENUM('ADMIN','CLASS_COORDINATOR','TEACHER') NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ===================== ACADEMIC YEAR =====================
CREATE TABLE academic_years (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    label       VARCHAR(20) NOT NULL UNIQUE,
    is_current  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE UNIQUE INDEX ux_academic_years_current
    ON academic_years ((CASE WHEN is_current THEN 1 ELSE NULL END));

-- ===================== CLASSES & COORDINATORS =====================
CREATE TABLE classes (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    academic_year_id  BIGINT NOT NULL,
    name              VARCHAR(50) NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_classes_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT ux_classes_name_year UNIQUE (academic_year_id, name)
) ENGINE=InnoDB;

CREATE TABLE class_coordinators (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id    BIGINT NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cc_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT fk_cc_user  FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ===================== STUDENTS =====================
CREATE TABLE students (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id    BIGINT NOT NULL,
    roll_no     VARCHAR(20) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    batch_label VARCHAR(20),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT ux_students_roll UNIQUE (class_id, roll_no)
) ENGINE=InnoDB;

-- ===================== SUBJECTS & BATCHES =====================
CREATE TABLE subjects (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id    BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    type        ENUM('TH','PR') NOT NULL,
    created_by  BIGINT NOT NULL,
    CONSTRAINT fk_subjects_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT fk_subjects_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT ux_subjects_name_class UNIQUE (class_id, name)
) ENGINE=InnoDB;

-- Every subject (TH or PR) has at least one batch row.
-- TH subjects get a single auto-created sentinel batch labeled 'Entire Class'
-- (is_whole_class = TRUE) so batch_id is NEVER NULL anywhere downstream.
CREATE TABLE batches (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id     BIGINT NOT NULL,
    label          VARCHAR(20) NOT NULL,
    is_whole_class BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_batches_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT ux_batches_label UNIQUE (subject_id, label)
) ENGINE=InnoDB;

-- ===================== ASSIGNMENTS (the ABAC core) =====================
-- Unified model: every assignment (Theory or Practical) is teacher -> class -> subject -> batch.
-- For TH subjects, batch_id points at that subject's sentinel 'Entire Class' batch row,
-- so this single table covers both cases with no NULLs and no separate Theory/Practical tables.
CREATE TABLE teacher_assignments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id  BIGINT NOT NULL,
    class_id    BIGINT NOT NULL,
    subject_id  BIGINT NOT NULL,
    batch_id    BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ta_teacher  FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT fk_ta_class    FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT fk_ta_subject  FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_ta_batch    FOREIGN KEY (batch_id) REFERENCES batches(id),
    CONSTRAINT fk_ta_assigner FOREIGN KEY (assigned_by) REFERENCES users(id),
    CONSTRAINT ux_ta UNIQUE (teacher_id, class_id, subject_id, batch_id)
) ENGINE=InnoDB;

-- ===================== ATTENDANCE (session lock + records) =====================
CREATE TABLE attendance_sessions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id     BIGINT NOT NULL,
    subject_id   BIGINT NOT NULL,
    batch_id     BIGINT NOT NULL,
    session_date DATE NOT NULL,
    slot         TINYINT NOT NULL,
    locked_by    BIGINT NOT NULL,
    locked_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted    BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMP NULL,
    CONSTRAINT fk_as_class   FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT fk_as_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_as_batch   FOREIGN KEY (batch_id) REFERENCES batches(id),
    CONSTRAINT fk_as_teacher FOREIGN KEY (locked_by) REFERENCES users(id),
    -- atomic DB-level guarantee against duplicate sessions — reliable because batch_id is
    -- never NULL (TH sessions use the sentinel batch), avoiding MySQL's multi-NULL unique-index gap
    CONSTRAINT ux_session UNIQUE (class_id, subject_id, batch_id, session_date, slot)
) ENGINE=InnoDB;

CREATE TABLE attendance_records (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT NOT NULL,
    student_id  BIGINT NOT NULL,
    status      ENUM('PRESENT','ABSENT') NOT NULL,
    CONSTRAINT fk_ar_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id),
    CONSTRAINT fk_ar_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT ux_ar UNIQUE (session_id, student_id)
) ENGINE=InnoDB;

CREATE INDEX ix_sessions_lookup ON attendance_sessions (class_id, subject_id, batch_id, session_date);
CREATE INDEX ix_records_student ON attendance_records (student_id);