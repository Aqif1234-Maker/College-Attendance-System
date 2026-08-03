import { useMutation } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function generateReport(filter) {
  const response = await axiosClient.post('/reports/generate', filter);
  return response.data.data;
}

export async function fetchReportSummary(filter) {
  const response = await axiosClient.post('/reports/summary', filter);
  return response.data.data;
}

export async function exportReportExcel(filter) {
  const response = await axiosClient.post('/reports/export/excel', filter, { responseType: 'blob' });
  return response.data;
}

export async function exportReportPdf(filter) {
  const response = await axiosClient.post('/reports/export/pdf', filter, { responseType: 'blob' });
  return response.data;
}

// Reports are filter-driven, on-demand requests rather than cached GET data,
// so these are mutations (triggered by "Generate Report") rather than queries.
export function useGenerateReport() {
  return useMutation({ mutationFn: generateReport });
}

export function useReportSummary() {
  return useMutation({ mutationFn: fetchReportSummary });
}

export function useExportReportExcel() {
  return useMutation({ mutationFn: exportReportExcel });
}

export function useExportReportPdf() {
  return useMutation({ mutationFn: exportReportPdf });
}