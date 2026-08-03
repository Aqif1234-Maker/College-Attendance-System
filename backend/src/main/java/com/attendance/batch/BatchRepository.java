package com.attendance.batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    List<Batch> findBySubjectId(Long subjectId);

    boolean existsBySubjectIdAndLabelIgnoreCase(Long subjectId, String label);

    Optional<Batch> findBySubjectIdAndWholeClassTrue(Long subjectId);
}
