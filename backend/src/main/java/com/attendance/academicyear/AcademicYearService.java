package com.attendance.academicyear;

import com.attendance.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final AcademicYearMapper academicYearMapper;

    public AcademicYearService(AcademicYearRepository academicYearRepository, AcademicYearMapper academicYearMapper) {
        this.academicYearRepository = academicYearRepository;
        this.academicYearMapper = academicYearMapper;
    }

    public AcademicYearDto create(CreateAcademicYearRequest request) {
        if (academicYearRepository.existsByLabel(request.label())) {
            throw new ApiException(HttpStatus.CONFLICT, "Academic year '" + request.label() + "' already exists");
        }

        AcademicYear year = new AcademicYear();
        year.setLabel(request.label());

        // The very first academic year created becomes current automatically;
        // subsequent years must be explicitly promoted via setCurrent().
        boolean isFirst = academicYearRepository.count() == 0;
        year.setCurrent(isFirst);

        return academicYearMapper.toDto(academicYearRepository.save(year));
    }

    @Transactional(readOnly = true)
    public List<AcademicYearDto> list() {
        return academicYearRepository.findAll().stream()
                .map(academicYearMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearDto getCurrent() {
        AcademicYear current = academicYearRepository.findByCurrentTrue()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No academic year is currently marked as current. An Admin must set one."));
        return academicYearMapper.toDto(current);
    }

    public AcademicYearDto setCurrent(Long id) {
        AcademicYear target = academicYearRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Academic year not found"));

        if (target.isCurrent()) {
            return academicYearMapper.toDto(target);
        }

        // Clear the old current row at the DB level and flush it before promoting the new one.
        // This avoids a transient unique-index conflict on the "exactly one current year" rule.
        academicYearRepository.clearCurrentExcept(target.getId());
        academicYearRepository.flush();
        target.setCurrent(true);
        return academicYearMapper.toDto(academicYearRepository.saveAndFlush(target));
    }
}
