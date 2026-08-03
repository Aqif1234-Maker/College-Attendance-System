import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchClasses(academicYearId) {
  const response = await axiosClient.get('/classes', {
    params: academicYearId ? { academicYearId } : {}
  });
  return response.data.data;
}

export async function fetchMyCoordinatedClasses(academicYearId) {
  const response = await axiosClient.get('/classes/my-coordinated', {
    params: academicYearId ? { academicYearId } : {}
  });
  return response.data.data;
}

export async function fetchClass(id) {
  const response = await axiosClient.get(`/classes/${id}`);
  return response.data.data;
}

export async function createClass(payload) {
  const response = await axiosClient.post('/classes', payload);
  return response.data.data;
}

export async function assignCoordinator(classId, userId) {
  const response = await axiosClient.put(`/classes/${classId}/coordinator`, { userId });
  return response.data.data;
}

export function useClasses(academicYearId) {
  return useQuery({
    queryKey: ['classes', academicYearId ?? 'all'],
    queryFn: () => fetchClasses(academicYearId)
  });
}

export function useMyCoordinatedClasses(academicYearId, enabled = true) {
  return useQuery({
    queryKey: ['classes', 'myCoordinated', academicYearId ?? 'all'],
    queryFn: () => fetchMyCoordinatedClasses(academicYearId),
    enabled
  });
}

export function useCreateClass() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createClass,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['classes'] })
  });
}

export function useAssignCoordinator() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ classId, userId }) => assignCoordinator(classId, userId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['classes'] })
  });
}
