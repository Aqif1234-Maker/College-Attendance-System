package com.attendance.batch;

import com.attendance.subject.Subject;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-02T12:50:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class BatchMapperImpl implements BatchMapper {

    @Override
    public BatchDto toDto(Batch batch) {
        if ( batch == null ) {
            return null;
        }

        Long subjectId = null;
        Long id = null;
        String label = null;
        boolean wholeClass = false;

        subjectId = batchSubjectId( batch );
        id = batch.getId();
        label = batch.getLabel();
        wholeClass = batch.isWholeClass();

        BatchDto batchDto = new BatchDto( id, subjectId, label, wholeClass );

        return batchDto;
    }

    private Long batchSubjectId(Batch batch) {
        if ( batch == null ) {
            return null;
        }
        Subject subject = batch.getSubject();
        if ( subject == null ) {
            return null;
        }
        Long id = subject.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
