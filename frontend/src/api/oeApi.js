import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from './axiosClient';

// ---- Teaching Slots (Coordinator: assign/reassign; Teacher: check my slots) ----

export async function fetchSlotsBySemester(semesterId) {
  const response = await axiosClient.get('/oe/teaching-slots', { params: { semesterId } });
  return response.data.data;
}

export async function fetchMySlots() {
  const response = await axiosClient.get('/oe/teaching-slots/my-slots');
  return response.data.data;
}

export async function assignSlot(payload) {
  const response = await axiosClient.post('/oe/teaching-slots', payload);
  return response.data.data;
}

export async function reassignSlot(id, payload) {
  const response = await axiosClient.put(`/oe/teaching-slots/${id}`, payload);
  return response.data.data;
}

export function useSlotsBySemester(semesterId) {
  return useQuery({
    queryKey: ['oe', 'slots', semesterId],
    queryFn: () => fetchSlotsBySemester(semesterId),
    enabled: Boolean(semesterId)
  });
}

export function useMySlots() {
  return useQuery({
    queryKey: ['oe', 'slots', 'mine'],
    queryFn: fetchMySlots
  });
}

export function useAssignSlot() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: assignSlot,
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['oe', 'slots', variables.semesterId] })
  });
}

export function useReassignSlot() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => reassignSlot(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['oe', 'slots'] })
  });
}

// ---- OE Subjects (Teacher: create, once holding a slot) ----

export async function createOeSubject(payload) {
  const response = await axiosClient.post('/oe/subjects', payload);
  return response.data.data;
}

export async function fetchOeSubjectBySlot(slotId) {
  const response = await axiosClient.get('/oe/subjects/by-slot', { params: { slotId } });
  return response.data.data;
}

export function useCreateOeSubject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createOeSubject,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['oe'] })
  });
}

export function useOeSubjectBySlot(slotId) {
  return useQuery({
    queryKey: ['oe', 'subject', 'bySlot', slotId],
    queryFn: () => fetchOeSubjectBySlot(slotId),
    enabled: Boolean(slotId),
    retry: false // 404 is an expected "not created yet" response, not a real error
  });
}

// ---- OE Enrollments (Teacher: checkbox-enroll existing students) ----

export async function fetchEnrollments(oeSubjectId) {
  const response = await axiosClient.get('/oe/enrollments', { params: { oeSubjectId } });
  return response.data.data;
}

export async function enrollStudents(payload) {
  const response = await axiosClient.post('/oe/enrollments', payload);
  return response.data.data;
}

export async function unenrollStudent(oeSubjectId, studentId) {
  await axiosClient.delete(`/oe/enrollments/${oeSubjectId}/${studentId}`);
}

export function useEnrollments(oeSubjectId) {
  return useQuery({
    queryKey: ['oe', 'enrollments', oeSubjectId],
    queryFn: () => fetchEnrollments(oeSubjectId),
    enabled: Boolean(oeSubjectId)
  });
}

export function useEnrollStudents() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: enrollStudents,
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['oe', 'enrollments', variables.oeSubjectId] })
  });
}

export function useUnenrollStudent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ oeSubjectId, studentId }) => unenrollStudent(oeSubjectId, studentId),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: ['oe', 'enrollments', variables.oeSubjectId] })
  });
}