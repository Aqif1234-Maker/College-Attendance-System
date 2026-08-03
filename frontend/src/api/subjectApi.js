import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchSubjectsByClass(classId) {
  const response = await axiosClient.get('/subjects', { params: { classId } });
  return response.data.data;
}

export async function createSubject(payload) {
  const response = await axiosClient.post('/subjects', payload);
  return response.data.data;
}

export async function updateSubject(id, payload) {
  const response = await axiosClient.put(`/subjects/${id}`, payload);
  return response.data.data;
}

export async function deleteSubject(id) {
  await axiosClient.delete(`/subjects/${id}`);
}

export function useSubjectsByClass(classId) {
  return useQuery({
    queryKey: ['subjects', classId],
    queryFn: () => fetchSubjectsByClass(classId),
    enabled: Boolean(classId)
  });
}

export function useCreateSubject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createSubject,
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['subjects', variables.classId] })
  });
}

export function useUpdateSubject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => updateSubject(id, payload),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['subjects', variables.payload.classId] })
  });
}

export function useDeleteSubject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteSubject,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['subjects'] })
  });
}