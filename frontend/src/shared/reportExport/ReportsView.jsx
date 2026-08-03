import { useState } from 'react';
import DependentFilter from '../DependentFilter/DependentFilter';
import { useDependentFilter } from '../DependentFilter/useDependentFilter';
import { useGenerateReport, useReportSummary, useExportReportExcel, useExportReportPdf } from '../../api/reportApi';
import { REPORT_PRESETS } from '../utils/attendanceStatus';
import AttendanceDistributionChart from '../charts/AttendanceDistributionChart';
import SubjectWiseBarChart from '../charts/SubjectWiseBarChart';
import { downloadBlob } from './exportUtils';
import Card from '../ui/Card';
import Table from '../ui/Table';
import Badge from '../ui/Badge';
import Button from '../ui/Button';

function daysAgoIso(days) {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString().slice(0, 10);
}

/**
 * The single Reports view used by both Teacher and Coordinator pages (spec §8:
 * "same shared filtering component/service, not duplicated"). Both feature-level
 * pages are now thin wrappers around this.
 */
export default function ReportsView() {
  const filter = useDependentFilter();

  const [fromDate, setFromDate] = useState(daysAgoIso(30));
  const [toDate, setToDate] = useState(new Date().toISOString().slice(0, 10));
  const [preset, setPreset] = useState('ALL');
  const [minPercent, setMinPercent] = useState('');
  const [maxPercent, setMaxPercent] = useState('');
  const [rows, setRows] = useState([]);
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');

  const generateMutation = useGenerateReport();
  const summaryMutation = useReportSummary();
  const exportExcelMutation = useExportReportExcel();
  const exportPdfMutation = useExportReportPdf();

  function buildFilterPayload() {
    return {
      classId: filter.classId,
      subjectId: filter.subjectId,
      batchId: filter.batchId,
      fromDate,
      toDate,
      minPercent: minPercent === '' ? null : Number(minPercent),
      maxPercent: maxPercent === '' ? null : Number(maxPercent),
      preset
    };
  }

  async function handleGenerate() {
    setError('');
    try {
      const payload = buildFilterPayload();
      const [reportRows, reportSummary] = await Promise.all([
        generateMutation.mutateAsync(payload),
        summaryMutation.mutateAsync(payload)
      ]);
      setRows(reportRows);
      setSummary(reportSummary);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not generate report.');
    }
  }

  async function handleExportExcel() {
    const blob = await exportExcelMutation.mutateAsync(buildFilterPayload());
    downloadBlob(blob, 'attendance-report.xlsx');
  }

  async function handleExportPdf() {
    const blob = await exportPdfMutation.mutateAsync(buildFilterPayload());
    downloadBlob(blob, 'attendance-report.pdf');
  }

  const canGenerate = filter.isComplete && Boolean(fromDate) && Boolean(toDate);

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-gray-900">Reports</h1>
          <p className="text-sm text-gray-500 mt-1">Attendance percentage and analytics for your assigned classes.</p>
        </div>
        {rows.length > 0 && (
          <div className="flex gap-2">
            <Button variant="secondary" onClick={handleExportExcel} loading={exportExcelMutation.isPending}>
              Export Excel
            </Button>
            <Button variant="secondary" onClick={handleExportPdf} loading={exportPdfMutation.isPending}>
              Export PDF
            </Button>
            <Button variant="secondary" onClick={() => window.print()}>
              Print Report
            </Button>
          </div>
        )}
      </div>

      <DependentFilter filter={filter} />

      <Card title="Filters">
        <div className="flex flex-wrap gap-4 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">From</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">To</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Preset</label>
            <select
              value={preset}
              onChange={(e) => setPreset(e.target.value)}
              className="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              {REPORT_PRESETS.map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Min %</label>
            <input
              type="number"
              min="0"
              max="100"
              value={minPercent}
              onChange={(e) => setMinPercent(e.target.value)}
              className="w-20 rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Max %</label>
            <input
              type="number"
              min="0"
              max="100"
              value={maxPercent}
              onChange={(e) => setMaxPercent(e.target.value)}
              className="w-20 rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <Button onClick={handleGenerate} disabled={!canGenerate} loading={generateMutation.isPending}>
            Generate Report
          </Button>
        </div>
      </Card>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-xl px-4 py-3">{error}</div>}

      {summary && (
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          <SummaryCard label="Total Students" value={summary.totalStudents} />
          <SummaryCard label="Average Attendance" value={`${summary.averageAttendance}%`} />
          <SummaryCard label="Below Threshold" value={summary.studentsBelowThreshold} />
          <SummaryCard label="Highest" value={`${summary.highestAttendance}%`} />
          <SummaryCard label="Lowest" value={`${summary.lowestAttendance}%`} />
        </div>
      )}

      {rows.length > 0 && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card title="Attendance Distribution">
              <AttendanceDistributionChart rows={rows} />
            </Card>
            <Card title="Subject-wise Attendance">
              <SubjectWiseBarChart rows={rows} />
            </Card>
          </div>

          <Card title="Report">
            <Table
              columns={[
                { key: 'rollNo', header: 'Roll No' },
                { key: 'studentName', header: 'Student Name' },
                { key: 'subjectName', header: 'Subject' },
                { key: 'classesConducted', header: 'Classes Conducted' },
                { key: 'classesAttended', header: 'Classes Attended' },
                { key: 'percentage', header: 'Attendance %', render: (row) => `${row.percentage}%` },
                { key: 'status', header: 'Status', render: (row) => <Badge status={row.status} /> }
              ]}
              rows={rows.map((r) => ({ ...r, id: r.studentId }))}
            />
          </Card>
        </>
      )}
    </div>
  );
}

function SummaryCard({ label, value }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-4">
      <div className="text-xs font-medium text-gray-500">{label}</div>
      <div className="text-xl font-semibold text-gray-900 mt-1">{value}</div>
    </div>
  );
}