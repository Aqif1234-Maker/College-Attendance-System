package com.attendance.class_;

import com.attendance.academicyear.AcademicYear;
import com.attendance.academicyear.AcademicYearRepository;
import com.attendance.common.ApiException;
import com.attendance.user.Role;
import com.attendance.user.User;
import com.attendance.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassCoordinatorRepository classCoordinatorRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository userRepository;
    private final ClassMapper classMapper;

    public ClassService(
            ClassRepository classRepository,
            ClassCoordinatorRepository classCoordinatorRepository,
            AcademicYearRepository academicYearRepository,
            UserRepository userRepository,
            ClassMapper classMapper) {
        this.classRepository = classRepository;
        this.classCoordinatorRepository = classCoordinatorRepository;
        this.academicYearRepository = academicYearRepository;
        this.userRepository = userRepository;
        this.classMapper = classMapper;
    }

    public ClassDto create(CreateClassRequest request) {
        AcademicYear year = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Academic year not found"));

        if (classRepository.existsByAcademicYearIdAndNameIgnoreCase(year.getId(), request.name())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Class '" + request.name() + "' already exists for this academic year");
        }

        ClassEntity classEntity = new ClassEntity();
        classEntity.setAcademicYear(year);
        classEntity.setName(request.name());

        return classMapper.toBaseDto(classRepository.save(classEntity));
    }

    @Transactional(readOnly = true)
    public List<ClassDto> list(Long academicYearId) {
        List<ClassEntity> classes = (academicYearId == null)
                ? classRepository.findAll()
                : classRepository.findByAcademicYearId(academicYearId);

        return classes.stream()
                .map(c -> classMapper.toDto(c, classCoordinatorRepository.findByClassEntityId(c.getId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassDto> listCoordinatedBy(Long userId, Long academicYearId) {
        List<ClassCoordinator> coordinators = (academicYearId == null)
                ? classCoordinatorRepository.findByUserId(userId)
                : classCoordinatorRepository.findByUserIdAndClassEntityAcademicYearId(userId, academicYearId);

        return coordinators.stream()
                .map(coordinator -> classMapper.toDto(coordinator.getClassEntity(), coordinator))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClassDto get(Long id) {
        ClassEntity classEntity = findEntity(id);
        return classMapper.toDto(classEntity, classCoordinatorRepository.findByClassEntityId(id).orElse(null));
    }

    public ClassDto assignCoordinator(Long classId, AssignCoordinatorRequest request) {
        ClassEntity classEntity = findEntity(classId);

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.CLASS_COORDINATOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only a user with the CLASS_COORDINATOR role can be assigned as a coordinator");
        }

        if (!user.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This user's account is deactivated");
        }

        // One class = one coordinator. Check first for a clean error message; the DB unique
        // constraint on class_id is still the final source of truth against races.
        classCoordinatorRepository.findByClassEntityId(classId).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This class already has a coordinator assigned (" + existing.getUser().getFullName() + ")");
        });

        ClassCoordinator coordinator = new ClassCoordinator();
        coordinator.setClassEntity(classEntity);
        coordinator.setUser(user);

        ClassCoordinator saved = classCoordinatorRepository.save(coordinator);
        return classMapper.toDto(classEntity, saved);
    }

    private ClassEntity findEntity(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));
    }
}
