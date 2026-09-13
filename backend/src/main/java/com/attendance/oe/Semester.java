package com.attendance.oe;

import com.attendance.academicyear.AcademicYear;
import com.attendance.class_.ClassEntity;
import com.attendance.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false, length = 30)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SemesterStatus status = SemesterStatus.ACTIVE;

    // The two divisions this semester's OE setup spans (e.g. TE-A, TE-B). Combined-mode
    // subjects draw their roster from both via oe_enrollments; Separate-mode subjects use
    // whichever one matches the slot's SEPARATE_A/SEPARATE_B mode. Both required — OE has
    // no meaning without exactly two divisions per the poster's design.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_a_id", nullable = false)
    private ClassEntity classA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_b_id", nullable = false)
    private ClassEntity classB;

    @Column(name = "closed_at")
    private Instant closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private User closedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    public Semester() {
    }

    public Long getId() {
        return id;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SemesterStatus getStatus() {
        return status;
    }

    public void setStatus(SemesterStatus status) {
        this.status = status;
    }

    public ClassEntity getClassA() {
        return classA;
    }

    public void setClassA(ClassEntity classA) {
        this.classA = classA;
    }

    public ClassEntity getClassB() {
        return classB;
    }

    public void setClassB(ClassEntity classB) {
        this.classB = classB;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}