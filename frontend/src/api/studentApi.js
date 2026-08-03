import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchStudentsByClass(classId, batchLabel) {
  const response = await axiosClient.get('/students', {
    params: batchLabel ? { classId, batchLabel } : { classId }
  });
  return response.data.data;
}

export async function createStudent(payload) {
  const response = await axiosClient.post('/students', payload);
  return response.data.data;
}

export async function updateStudent(id, payload) {
  const response = await axiosClient.put(`/students/${id}`, payload);
  return response.data.data;
}

export async function deactivateStudent(id) {
  await axiosClient.patch(`/students/${id}/deactivate`);
}

export function useStudentsByClass(classId, batchLabel) {
  return useQuery({
    queryKey: ['students', classId, batchLabel ?? 'all'],
    queryFn: () => fetchStudentsByClass(classId, batchLabel),
    enabled: Boolean(classId)
  });
}

export function useCreateStudent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createStudent,
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['students', variables.classId] })
  });
}

export function useUpdateStudent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => updateStudent(id, payload),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['students', variables.payload.classId] })
  });
}

export function useDeactivateStudent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deactivateStudent,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['students'] })
  });
}