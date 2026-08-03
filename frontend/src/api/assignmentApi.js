import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchMyClasses() {
  const response = await axiosClient.get('/assignments/my-classes');
  return response.data.data;
}

export async function fetchMySubjects(classId) {
  const response = await axiosClient.get('/assignments/my-subjects', { params: { classId } });
  return response.data.data;
}

export async function fetchMyBatches(classId, subjectId) {
  const response = await axiosClient.get('/assignments/my-batches', { params: { classId, subjectId } });
  return response.data.data;
}

export function useMyClasses() {
  return useQuery({ queryKey: ['assignments', 'myClasses'], queryFn: fetchMyClasses });
}

export function useMySubjects(classId) {
  return useQuery({
    queryKey: ['assignments', 'mySubjects', classId],
    queryFn: () => fetchMySubjects(classId),
    enabled: Boolean(classId)
  });
}

export function useMyBatches(classId, subjectId) {
  return useQuery({
    queryKey: ['assignments', 'myBatches', classId, subjectId],
    queryFn: () => fetchMyBatches(classId, subjectId),
    enabled: Boolean(classId) && Boolean(subjectId)
  });
}

// --- Coordinator-side: assign/remove teachers (Faculty Assignment page) ---

export async function fetchAssignmentsForTeacher(teacherId) {
  const response = await axiosClient.get('/assignments', { params: { teacherId } });
  return response.data.data;
}

export async function fetchAssignmentsForClass(classId) {
  const response = await axiosClient.get('/assignments/by-class', { params: { classId } });
  return response.data.data;
}

export async function assignTeacher(payload) {
  const response = await axiosClient.post('/assignments', payload);
  return response.data.data;
}

export async function removeAssignment(id) {
  await axiosClient.delete(`/assignments/${id}`);
}

export function useAssignmentsForTeacher(teacherId) {
  return useQuery({
    queryKey: ['assignments', 'byTeacher', teacherId],
    queryFn: () => fetchAssignmentsForTeacher(teacherId),
    enabled: Boolean(teacherId)
  });
}

export function useAssignmentsForClass(classId) {
  return useQuery({
    queryKey: ['assignments', 'byClass', classId],
    queryFn: () => fetchAssignmentsForClass(classId),
    enabled: Boolean(classId)
  });
}

export function useAssignTeacher() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: assignTeacher,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['assignments', 'byTeacher', variables.teacherId] });
      queryClient.invalidateQueries({ queryKey: ['assignments', 'byClass', variables.classId] });
      queryClient.invalidateQueries({ queryKey: ['assignments', 'myClasses'] });
    }
  });
}

export function useRemoveAssignment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeAssignment,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['assignments'] })
  });
}
