import { create } from 'zustand';

const STORAGE_KEY = 'attendance_auth';

function loadPersisted() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function persist(state) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function clearPersisted() {
  sessionStorage.removeItem(STORAGE_KEY);
}

const persisted = loadPersisted();

export const useAuthStore = create((set) => ({
  token: persisted?.token ?? null,
  userId: persisted?.userId ?? null,
  username: persisted?.username ?? null,
  fullName: persisted?.fullName ?? null,
  role: persisted?.role ?? null,

  login: (loginResponse) => {
    const state = {
      token: loginResponse.token,
      userId: loginResponse.userId,
      username: loginResponse.username,
      fullName: loginResponse.fullName,
      role: loginResponse.role
    };
    persist(state);
    set(state);
  },

  logout: () => {
    clearPersisted();
    set({ token: null, userId: null, username: null, fullName: null, role: null });
  },

  isAuthenticated: () => {
    return sessionStorage.getItem(STORAGE_KEY) !== null;
  }
}));