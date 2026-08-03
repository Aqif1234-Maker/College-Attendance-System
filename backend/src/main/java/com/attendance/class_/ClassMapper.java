package com.attendance.class_;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassMapper {

    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLabel", source = "academicYear.label")
    @Mapping(target = "coordinatorId", ignore = true)
    @Mapping(target = "coordinatorName", ignore = true)
    ClassDto toBaseDto(ClassEntity classEntity);

    default ClassDto toDto(ClassEntity classEntity, ClassCoordinator coordinator) {
        ClassDto base = toBaseDto(classEntity);
        if (coordinator == null) {
            return base;
        }
        return new ClassDto(
                base.id(),
                base.name(),
                base.academicYearId(),
                base.academicYearLabel(),
                coordinator.getUser().getId(),
                coordinator.getUser().getFullName()
        );
    }
}