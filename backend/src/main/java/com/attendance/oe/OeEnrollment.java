package com.attendance.oe;

import com.attendance.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "oe_enrollments")
public class OeEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oe_subject_id", nullable = false)
    private OeSubject oeSubject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    public OeEnrollment() {
    }

    public Long getId() {
        return id;
    }

    public OeSubject getOeSubject() {
        return oeSubject;
    }

    public void setOeSubject(OeSubject oeSubject) {
        this.oeSubject = oeSubject;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }
}