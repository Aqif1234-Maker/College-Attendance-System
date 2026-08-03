import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchUsers(role) {
  const response = await axiosClient.get('/users', { params: role ? { role } : {} });
  return response.data.data;
}

export async function fetchUser(id) {
  const response = await axiosClient.get(`/users/${id}`);
  return response.data.data;
}

export async function createUser(payload) {
  const response = await axiosClient.post('/users', payload);
  return response.data.data;
}

export async function activateUser(id) {
  const response = await axiosClient.patch(`/users/${id}/activate`);
  return response.data.data;
}

export async function deactivateUser(id) {
  const response = await axiosClient.patch(`/users/${id}/deactivate`);
  return response.data.data;
}

export async function resetUserPassword(id, newPassword) {
  const response = await axiosClient.patch(`/users/${id}/reset-password`, { newPassword });
  return response.data.data;
}

export function useUsers(role) {
  return useQuery({
    queryKey: ['users', role ?? 'all'],
    queryFn: () => fetchUsers(role)
  });
}

export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createUser,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] })
  });
}

export function useSetUserActive() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }) => (active ? activateUser(id) : deactivateUser(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] })
  });
}

export function useResetUserPassword() {
  return useMutation({
    mutationFn: ({ id, newPassword }) => resetUserPassword(id, newPassword)
  });
}