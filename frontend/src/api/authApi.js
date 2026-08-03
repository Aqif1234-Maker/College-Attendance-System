import axiosClient from './axiosClient';

export async function login(username, password) {
  const response = await axiosClient.post('/auth/login', { username, password });
  return response.data.data;
}