package com.attendance.batch;

import com.attendance.common.ApiException;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.subject.SubjectType;
import com.attendance.subject.TeachingStructureCleanupService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BatchService {

    private final BatchRepository batchRepository;
    private final SubjectRepository subjectRepository;
    private final BatchMapper batchMapper;
    private final TeachingStructureCleanupService teachingStructureCleanupService;

    public BatchService(
            BatchRepository batchRepository,
            SubjectRepository subjectRepository,
            BatchMapper batchMapper,
            TeachingStructureCleanupService teachingStructureCleanupService) {
        this.batchRepository = batchRepository;
        this.subjectRepository = subjectRepository;
        this.batchMapper = batchMapper;
        this.teachingStructureCleanupService = teachingStructureCleanupService;
    }

    public BatchDto create(CreateBatchRequest request) {
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));

        if (subject.getType() != SubjectType.PR) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Batches can only be added to Practical (PR) subjects; Theory subjects use the Entire Class batch");
        }

        if (batchRepository.existsBySubjectIdAndLabelIgnoreCase(subject.getId(), request.label())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Batch '" + request.label() + "' already exists for this subject");
        }

        Batch batch = new Batch();
        batch.setSubject(subject);
        batch.setLabel(request.label());
        batch.setWholeClass(false);

        return batchMapper.toDto(batchRepository.save(batch));
    }

    @Transactional(readOnly = true)
    public List<BatchDto> listBySubject(Long subjectId) {
        return batchRepository.findBySubjectId(subjectId).stream()
                .map(batchMapper::toDto)
                .toList();
    }

    public void delete(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Batch not found"));

        if (batch.isWholeClass()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "The Entire Class batch is managed automatically and cannot be deleted directly");
        }

        teachingStructureCleanupService.cleanupBatch(batch);
        batchRepository.delete(batch);
    }
}
