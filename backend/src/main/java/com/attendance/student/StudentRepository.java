package com.attendance.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByClassEntityId(Long classId);

    List<Student> findByClassEntityIdAndBatchLabel(Long classId, String batchLabel);

    boolean existsByClassEntityIdAndRollNoIgnoreCase(Long classId, String rollNo);
}