package com.attendance.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    List<TeacherAssignment> findByTeacherId(Long teacherId);

    List<TeacherAssignment> findByClassEntityId(Long classId);

    boolean existsByTeacherIdAndClassEntityIdAndSubjectIdAndBatchId(
            Long teacherId, Long classId, Long subjectId, Long batchId);

    @Query("""
            select ta.classEntity from TeacherAssignment ta
            where ta.teacher.id = :teacherId
            group by ta.classEntity
            """)
    List<com.attendance.class_.ClassEntity> findDistinctAssignedClasses(@Param("teacherId") Long teacherId);

    @Query("""
            select ta.subject from TeacherAssignment ta
            where ta.teacher.id = :teacherId and ta.classEntity.id = :classId
            group by ta.subject
            """)
    List<com.attendance.subject.Subject> findDistinctAssignedSubjects(
            @Param("teacherId") Long teacherId, @Param("classId") Long classId);

    @Query("""
            select ta.batch from TeacherAssignment ta
            where ta.teacher.id = :teacherId and ta.classEntity.id = :classId and ta.subject.id = :subjectId
            """)
    List<com.attendance.batch.Batch> findAssignedBatches(
            @Param("teacherId") Long teacherId, @Param("classId") Long classId, @Param("subjectId") Long subjectId);

    Optional<TeacherAssignment> findByTeacherIdAndClassEntityIdAndSubjectIdAndBatchId(
            Long teacherId, Long classId, Long subjectId, Long batchId);

    Optional<TeacherAssignment> findByClassEntityIdAndSubjectIdAndBatchId(
            Long classId, Long subjectId, Long batchId);

    void deleteBySubjectId(Long subjectId);

    void deleteByBatchId(Long batchId);
}
