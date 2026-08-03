import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function openAttendanceSession(payload) {
  const response = await axiosClient.post('/attendance/sessions', payload);
  return response.data.data;
}

export async function fetchAttendanceSession(id) {
  const response = await axiosClient.get(`/attendance/sessions/${id}`);
  return response.data.data;
}

export async function submitAttendance(payload) {
  const response = await axiosClient.post('/attendance/submit', payload);
  return response.data.data;
}

export async function fetchAttendanceHistory() {
  const response = await axiosClient.get('/attendance/history');
  return response.data.data;
}

export function useOpenAttendanceSession() {
  return useMutation({
    mutationFn: openAttendanceSession
  });
}

export function useAttendanceSession(id) {
  return useQuery({
    queryKey: ['attendance', 'session', id],
    queryFn: () => fetchAttendanceSession(id),
    enabled: Boolean(id)
  });
}

export function useSubmitAttendance() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: submitAttendance,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'session', data.id] });
      queryClient.invalidateQueries({ queryKey: ['attendance', 'history'] });
    }
  });
}

export function useAttendanceHistory() {
  return useQuery({
    queryKey: ['attendance', 'history'],
    queryFn: fetchAttendanceHistory
  });
}