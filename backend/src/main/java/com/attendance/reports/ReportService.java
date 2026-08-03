package com.attendance.reports;

import com.attendance.assignment.AssignmentLookupService;
import com.attendance.attendance.AttendanceRecord;
import com.attendance.attendance.AttendanceRecordRepository;
import com.attendance.attendance.AttendanceSession;
import com.attendance.attendance.AttendanceSessionRepository;
import com.attendance.attendance.AttendanceStatus;
import com.attendance.batch.Batch;
import com.attendance.batch.BatchRepository;
import com.attendance.common.ApiException;
import com.attendance.student.Student;
import com.attendance.student.StudentRepository;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final double EXCELLENT_THRESHOLD = 85.0;
    private static final double GOOD_THRESHOLD = 75.0;
    private static final double WARNING_THRESHOLD = 65.0;

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final BatchRepository batchRepository;
    private final AssignmentLookupService assignmentLookupService;

    public ReportService(
            AttendanceSessionRepository sessionRepository,
            AttendanceRecordRepository recordRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            BatchRepository batchRepository,
            AssignmentLookupService assignmentLookupService) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.assignmentLookupService = assignmentLookupService;
    }

    public List<ReportRowDto> generate(ReportFilterRequest request, Long teacherId) {
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subject not found"));

        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Batch not found"));

        assignmentLookupService.assertAssigned(teacherId, request.classId(), request.subjectId(), request.batchId());

        if (request.toDate().isBefore(request.fromDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "'To' date cannot be before 'From' date");
        }

        List<AttendanceSession> sessions = sessionRepository
                .findByClassEntityIdAndSubjectIdAndBatchIdAndSessionDateBetween(
                        request.classId(), request.subjectId(), request.batchId(),
                        request.fromDate(), request.toDate())
                .stream()
                .filter(AttendanceSession::isSubmitted)
                .toList();

        List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
        List<AttendanceRecord> records = sessionIds.isEmpty()
                ? List.of()
                : recordRepository.findBySessionIdIn(sessionIds);

        Map<Long, List<AttendanceRecord>> recordsByStudent = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        // A student "belongs" to this report row set if they belong to the class and,
        // for non-whole-class batches, their batchLabel matches this batch's label.
        List<Student> relevantStudents = studentRepository.findByClassEntityId(request.classId()).stream()
                .filter(Student::isActive)
                .filter(s -> batch.isWholeClass() || batch.getLabel().equalsIgnoreCase(s.getBatchLabel()))
                .toList();

        int conducted = sessions.size();

        List<ReportRowDto> rows = new ArrayList<>();
        for (Student student : relevantStudents) {
            List<AttendanceRecord> studentRecords = recordsByStudent.getOrDefault(student.getId(), List.of());
            int attended = (int) studentRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            double percentage = conducted == 0 ? 0.0 : (attended * 100.0) / conducted;

            rows.add(new ReportRowDto(
                    student.getId(), student.getRollNo(), student.getName(), subject.getName(),
                    conducted, attended, round1(percentage), statusFor(percentage)));
        }

        return applyFilter(rows, request);
    }

    public ReportSummaryDto summarize(List<ReportRowDto> rows) {
        if (rows.isEmpty()) {
            return new ReportSummaryDto(0, 0.0, 0, 0.0, 0.0);
        }

        double average = rows.stream().mapToDouble(ReportRowDto::percentage).average().orElse(0.0);
        int belowThreshold = (int) rows.stream().filter(r -> r.percentage() < GOOD_THRESHOLD).count();
        double highest = rows.stream().mapToDouble(ReportRowDto::percentage).max().orElse(0.0);
        double lowest = rows.stream().mapToDouble(ReportRowDto::percentage).min().orElse(0.0);

        return new ReportSummaryDto(rows.size(), round1(average), belowThreshold, round1(highest), round1(lowest));
    }

    private List<ReportRowDto> applyFilter(List<ReportRowDto> rows, ReportFilterRequest request) {
        List<ReportRowDto> filtered = rows;

        if (request.preset() != null) {
            filtered = switch (request.preset()) {
                case ALL -> filtered;
                case BELOW_75, DEFAULTERS -> filtered.stream().filter(r -> r.percentage() < GOOD_THRESHOLD).toList();
                case BELOW_65 -> filtered.stream().filter(r -> r.percentage() < WARNING_THRESHOLD).toList();
                case ABOVE_75 -> filtered.stream().filter(r -> r.percentage() >= GOOD_THRESHOLD).toList();
                case ABOVE_85, EXCELLENT -> filtered.stream().filter(r -> r.percentage() >= EXCELLENT_THRESHOLD).toList();
            };
        }

        if (request.minPercent() != null) {
            filtered = filtered.stream().filter(r -> r.percentage() >= request.minPercent()).toList();
        }
        if (request.maxPercent() != null) {
            filtered = filtered.stream().filter(r -> r.percentage() <= request.maxPercent()).toList();
        }

        return filtered;
    }

    private String statusFor(double percentage) {
        if (percentage >= EXCELLENT_THRESHOLD) return "EXCELLENT";
        if (percentage >= GOOD_THRESHOLD) return "GOOD";
        if (percentage >= WARNING_THRESHOLD) return "WARNING";
        return "DEFAULTER";
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}