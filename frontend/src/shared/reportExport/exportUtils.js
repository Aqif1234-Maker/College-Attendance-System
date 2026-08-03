import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

/**
 * Shared PDF/Excel/print export logic (spec §5.5), reused by both Teacher and
 * Coordinator Reports pages so export formatting never diverges between them.
 * These are CLIENT-SIDE exports of already-fetched rows — for the authoritative
 * server-generated files, use reportApi's exportReportExcel/exportReportPdf instead.
 * This module exists for a quick client-side export of whatever's currently on screen.
 */

const EXPORT_COLUMNS = [
  { key: 'rollNo', header: 'Roll No' },
  { key: 'studentName', header: 'Student Name' },
  { key: 'subjectName', header: 'Subject' },
  { key: 'classesConducted', header: 'Conducted' },
  { key: 'classesAttended', header: 'Attended' },
  { key: 'percentage', header: '%' },
  { key: 'status', header: 'Status' }
];

export function exportRowsToExcel(rows, filename = 'attendance-report.xlsx') {
  const worksheetData = rows.map((row) => ({
    'Roll No': row.rollNo,
    'Student Name': row.studentName,
    Subject: row.subjectName,
    'Classes Conducted': row.classesConducted,
    'Classes Attended': row.classesAttended,
    'Attendance %': row.percentage,
    Status: row.status
  }));

  const worksheet = XLSX.utils.json_to_sheet(worksheetData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Attendance Report');
  XLSX.writeFile(workbook, filename);
}

export function exportRowsToPdf(rows, filename = 'attendance-report.pdf') {
  const doc = new jsPDF({ orientation: 'landscape' });

  doc.setFontSize(16);
  doc.text('Attendance Report', 14, 16);

  autoTable(doc, {
    startY: 22,
    head: [EXPORT_COLUMNS.map((c) => c.header)],
    body: rows.map((row) => EXPORT_COLUMNS.map((c) => row[c.key])),
    styles: { fontSize: 9 },
    headStyles: { fillColor: [37, 99, 235] }
  });

  doc.save(filename);
}

export function printRows(rows) {
  const printWindow = window.open('', '_blank');
  if (!printWindow) return;

  const tableRows = rows
    .map(
      (row) => `
      <tr>
        <td>${row.rollNo}</td>
        <td>${row.studentName}</td>
        <td>${row.subjectName}</td>
        <td>${row.classesConducted}</td>
        <td>${row.classesAttended}</td>
        <td>${row.percentage}%</td>
        <td>${row.status}</td>
      </tr>`
    )
    .join('');

  printWindow.document.write(`
    <html>
      <head>
        <title>Attendance Report</title>
        <style>
          body { font-family: sans-serif; padding: 24px; }
          table { width: 100%; border-collapse: collapse; font-size: 13px; }
          th, td { border: 1px solid #e5e7eb; padding: 8px 10px; text-align: left; }
          th { background: #f9fafb; }
        </style>
      </head>
      <body>
        <h2>Attendance Report</h2>
        <table>
          <thead>
            <tr>${EXPORT_COLUMNS.map((c) => `<th>${c.header}</th>`).join('')}</tr>
          </thead>
          <tbody>${tableRows}</tbody>
        </table>
      </body>
    </html>
  `);

  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

/**
 * Downloads a file blob returned by the server-side export endpoints
 * (reportApi.exportReportExcel / exportReportPdf).
 */
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}