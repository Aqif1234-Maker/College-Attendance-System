import { useQuery } from '@tanstack/react-query';
import axiosClient from './axiosClient';

export async function fetchCurrentAcademicYear() {
  const response = await axiosClient.get('/academic-years/current');
  return response.data.data;
}

export function useCurrentAcademicYear() {
  return useQuery({
    queryKey: ['academicYear', 'current'],
    queryFn: fetchCurrentAcademicYear
  }); 
}