package com.attendance.oe;

import com.attendance.subject.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "oe_subjects")
public class OeSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, unique = true)
    private Subject subject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oe_teaching_slot_id", nullable = false, unique = true)
    private OeTeachingSlot oeTeachingSlot;

    @Column(name = "created_at")
    private Instant createdAt;

    public OeSubject() {
    }

    public Long getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public OeTeachingSlot getOeTeachingSlot() {
        return oeTeachingSlot;
    }

    public void setOeTeachingSlot(OeTeachingSlot oeTeachingSlot) {
        this.oeTeachingSlot = oeTeachingSlot;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}