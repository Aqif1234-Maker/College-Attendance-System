package com.attendance.reports;

import com.attendance.common.ApiResponse;
import com.attendance.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('TEACHER', 'CLASS_COORDINATOR')")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;
    private final CurrentUser currentUser;

    public ReportController(ReportService reportService, ReportExportService reportExportService, CurrentUser currentUser) {
        this.reportService = reportService;
        this.reportExportService = reportExportService;
        this.currentUser = currentUser;
    }

    @PostMapping("/generate")
    public ApiResponse<List<ReportRowDto>> generate(@Valid @RequestBody ReportFilterRequest request) {
        return ApiResponse.ok(reportService.generate(request, currentUser.getUserId()));
    }

    @PostMapping("/summary")
    public ApiResponse<ReportSummaryDto> summary(@Valid @RequestBody ReportFilterRequest request) {
        List<ReportRowDto> rows = reportService.generate(request, currentUser.getUserId());
        return ApiResponse.ok(reportService.summarize(rows));
    }

    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@Valid @RequestBody ReportFilterRequest request) {
        List<ReportRowDto> rows = reportService.generate(request, currentUser.getUserId());
        byte[] file = reportExportService.toExcel(rows);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("attendance-report.xlsx").build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@Valid @RequestBody ReportFilterRequest request) {
        List<ReportRowDto> rows = reportService.generate(request, currentUser.getUserId());
        byte[] file = reportExportService.toPdf(rows);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("attendance-report.pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}