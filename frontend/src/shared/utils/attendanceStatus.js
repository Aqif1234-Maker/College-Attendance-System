export const STATUS_THRESHOLDS = {
  EXCELLENT: 85,
  GOOD: 75,
  WARNING: 65
};

/**
 * Mirrors the backend's ReportService.statusFor() logic exactly — this is a display-only
 * convenience for client-side sorting/summaries; the authoritative status string always
 * comes from the API response (ReportRowDto.status), never recomputed and trusted blindly.
 */
export function statusForPercentage(percentage) {
  if (percentage >= STATUS_THRESHOLDS.EXCELLENT) return 'EXCELLENT';
  if (percentage >= STATUS_THRESHOLDS.GOOD) return 'GOOD';
  if (percentage >= STATUS_THRESHOLDS.WARNING) return 'WARNING';
  return 'DEFAULTER';
}

export const REPORT_PRESETS = [
  { value: 'ALL', label: 'Show All' },
  { value: 'BELOW_75', label: 'Below 75%' },
  { value: 'BELOW_65', label: 'Below 65%' },
  { value: 'ABOVE_75', label: 'Above 75%' },
  { value: 'ABOVE_85', label: 'Above 85%' },
  { value: 'DEFAULTERS', label: 'Defaulters' },
  { value: 'EXCELLENT', label: 'Excellent Attendance' }
];