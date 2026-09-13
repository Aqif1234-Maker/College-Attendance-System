package com.attendance.oe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OeTeachingSlotMapper {

    @Mapping(target = "semesterId", source = "semester.id")
    @Mapping(target = "semesterLabel", source = "semester.label")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    @Mapping(target = "assignedById", source = "assignedBy.id")
    @Mapping(target = "assignedByName", source = "assignedBy.fullName")
    @Mapping(target = "subjectCreated", ignore = true)
    OeTeachingSlotDto toBaseDto(OeTeachingSlot slot);

    default OeTeachingSlotDto toDto(OeTeachingSlot slot, boolean subjectCreated) {
        OeTeachingSlotDto base = toBaseDto(slot);
        return new OeTeachingSlotDto(
                base.id(), base.semesterId(), base.semesterLabel(), base.mode(),
                base.teacherId(), base.teacherName(), base.assignedById(), base.assignedByName(),
                subjectCreated
        );
    }
}