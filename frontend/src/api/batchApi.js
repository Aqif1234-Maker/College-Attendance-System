import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchBatchesBySubject(subjectId) {
  const response = await axiosClient.get('/batches', { params: { subjectId } });
  return response.data.data;
}

export async function createBatch(payload) {
  const response = await axiosClient.post('/batches', payload);
  return response.data.data;
}

export async function deleteBatch(id) {
  await axiosClient.delete(`/batches/${id}`);
}

export function useBatchesBySubject(subjectId) {
  return useQuery({
    queryKey: ['batches', subjectId],
    queryFn: () => fetchBatchesBySubject(subjectId),
    enabled: Boolean(subjectId)
  });
}

export function useCreateBatch() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createBatch,
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['batches', variables.subjectId] })
  });
}

export function useDeleteBatch() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteBatch,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['batches'] })
  });
}