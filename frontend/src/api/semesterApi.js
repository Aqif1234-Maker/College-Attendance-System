import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchSemesters(academicYearId) {
  const response = await axiosClient.get('/semesters', {
    params: academicYearId ? { academicYearId } : {}
  });
  return response.data.data;
}

export async function fetchSemester(id) {
  const response = await axiosClient.get(`/semesters/${id}`);
  return response.data.data;
}

export async function createSemester(payload) {
  const response = await axiosClient.post('/semesters', payload);
  return response.data.data;
}

export async function closeSemester(id) {
  const response = await axiosClient.patch(`/semesters/${id}/close`);
  return response.data.data;
}

export function useSemesters(academicYearId) {
  return useQuery({
    queryKey: ['semesters', academicYearId ?? 'all'],
    queryFn: () => fetchSemesters(academicYearId)
  });
}

export function useSemester(id) {
  return useQuery({
    queryKey: ['semesters', 'detail', id],
    queryFn: () => fetchSemester(id),
    enabled: Boolean(id)
  });
}

export function useCreateSemester() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createSemester,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['semesters'] })
  });
}

export function useCloseSemester() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: closeSemester,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['semesters'] })
  });
}