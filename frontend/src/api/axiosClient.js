import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: {
    'Content-Type': 'application/json'
  }
});

axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token invalid/expired: clear auth state and force back to login.
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    if (error.response?.status === 403) {
      // Server re-validated role/assignment and rejected this request —
      // per spec §1, the frontend must reflect this immediately, not retry silently.
      console.warn('Forbidden:', error.response.data?.message);
    }
    return Promise.reject(error);
  }
);

export default axiosClient;