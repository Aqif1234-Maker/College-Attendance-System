package com.attendance.subject;

import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.class_.ClassEntity;
import com.attendance.class_.ClassRepository;
import com.attendance.common.ApiException;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectService {

    private static final String WHOLE_CLASS_LABEL = "Entire Class";

    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final SubjectMapper subjectMapper;
    private final TeachingStructureCleanupService teachingStructureCleanupService;

    public SubjectService(
            SubjectRepository subjectRepository,
            ClassRepository classRepository,
            UserRepository userRepository,
            BatchRepository batchRepository,
            SubjectMapper subjectMapper,
            TeachingStructureCleanupService teachingStructureCleanupService) {
        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.subjectMapper = subjectMapper;
        this.teachingStructureCleanupService = teachingStructureCleanupService;
    }

    public SubjectDto create(CreateSubjectRequest request, Long creatorUserId) {
        ClassEntity classEntity = classRepository.findById(request.classId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Creating user not found"));

        if (subjectRepository.existsByClassEntityIdAndNameIgnoreCase(classEntity.getId(), request.name())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Subject '" + request.name() + "' already exists for this class");
        }

        Subject subject = new Subject();
        subject.setClassEntity(classEntity);
        subject.setName(request.name());
        subject.setType(request.type());
        subject.setCreatedBy(creator);

        Subject saved = subjectRepository.save(subject);

        // Every subject must have at least one batch row so attendance sessions and
        // teacher assignments never reference a null batch. Theory subjects get exactly
        // one auto-created sentinel batch representing the whole class.
        if (request.type() == com.attendance.subject.SubjectType.TH) {
            Batch wholeClassBatch = new Batch();
            wholeClassBatch.setSubject(saved);
            wholeClassBatch.setLabel(WHOLE_CLASS_LABEL);
            wholeClassBatch.setWholeClass(true);
            batchRepository.save(wholeClassBatch);
        }

        return subjectMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> listByClass(Long classId) {
        return subjectRepository.findByClassEntityId(classId).stream()
                .map(subjectMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectDto get(Long id) {
        return subjectMapper.toDto(findEntity(id));
    }

    public SubjectDto update(Long id, CreateSubjectRequest request, Long requesterUserId) {
        Subject subject = findEntity(id);

        assertCreator(subject, requesterUserId);

        if (!subject.getName().equalsIgnoreCase(request.name())
                && subjectRepository.existsByClassEntityIdAndNameIgnoreCase(subject.getClassEntity().getId(), request.name())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Subject '" + request.name() + "' already exists for this class");
        }

        if (subject.getType() != request.type()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Subject type cannot be changed after creation because it determines batch structure");
        }

        subject.setName(request.name());
        return subjectMapper.toDto(subject);
    }

    public void delete(Long id, Long requesterUserId) {
        Subject subject = findEntity(id);
        assertCreator(subject, requesterUserId);
        teachingStructureCleanupService.cleanupSubject(subject.getId());
        subjectRepository.delete(subject);
    }

    private void assertCreator(Subject subject, Long requesterUserId) {
        if (!subject.getCreatedBy().getId().equals(requesterUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the coordinator who created this subject can modify it");
        }
    }

    private Subject findEntity(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));
    }
}
