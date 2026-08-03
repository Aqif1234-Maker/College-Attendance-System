package com.attendance.reports;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportExportService {

    public byte[] toExcel(List<ReportRowDto> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Attendance Report");

            String[] headers = {"Roll No", "Student Name", "Subject", "Classes Conducted",
                    "Classes Attended", "Attendance %", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (ReportRowDto row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.rollNo());
                excelRow.createCell(1).setCellValue(row.studentName());
                excelRow.createCell(2).setCellValue(row.subjectName());
                excelRow.createCell(3).setCellValue(row.classesConducted());
                excelRow.createCell(4).setCellValue(row.classesAttended());
                excelRow.createCell(5).setCellValue(row.percentage());
                excelRow.createCell(6).setCellValue(row.status());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new com.attendance.common.ApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate Excel report");
        }
    }

    public byte[] toPdf(List<ReportRowDto> rows) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            document.add(new Paragraph("Attendance Report", titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);

            String[] headers = {"Roll No", "Student Name", "Subject", "Conducted", "Attended", "%", "Status"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, new Font(Font.HELVETICA, 10, Font.BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (ReportRowDto row : rows) {
                table.addCell(row.rollNo());
                table.addCell(row.studentName());
                table.addCell(row.subjectName());
                table.addCell(String.valueOf(row.classesConducted()));
                table.addCell(String.valueOf(row.classesAttended()));
                table.addCell(row.percentage() + "%");
                table.addCell(row.status());
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new com.attendance.common.ApiException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate PDF report");
        }
    }
}