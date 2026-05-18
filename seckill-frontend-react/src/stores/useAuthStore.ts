import { create } from 'zustand';
import { storage } from '../utils/storage';
import { authApi } from '../api/auth';
import { registerUnauthorizedHandler } from '../api/index';
import { LoginRequest, RegisterRequest } from '../types/user';

interface AuthState {
  token: string | null;
  username: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;

  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  checkAuth: () => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => {
  // 注册 401 回调：同步清除 Zustand 状态
  registerUnauthorizedHandler(() => {
    set({ token: null, username: null, isAuthenticated: false });
  });

  return {
  token: storage.getToken(),
  username: storage.getUsername(),
  isAuthenticated: !!storage.getToken(),
  loading: false,
  error: null,

  login: async (data: LoginRequest) => {
    set({ loading: true, error: null });
    try {
      const response = await authApi.login(data);
      const { token } = response.data;
      storage.setToken(token);
      storage.setUsername(data.username);
      set({
        token,
        username: data.username,
        isAuthenticated: true,
        loading: false,
      });
    } catch (error) {
      set({ loading: false, error: (error as Error).message });
      throw error;
    }
  },

  register: async (data: RegisterRequest) => {
    set({ loading: true, error: null });
    try {
      await authApi.register(data);
      set({ loading: false });
    } catch (error) {
      set({ loading: false, error: (error as Error).message });
      throw error;
    }
  },

  logout: () => {
    storage.clear();
    set({
      token: null,
      username: null,
      isAuthenticated: false,
    });
  },

  checkAuth: () => {
    const token = storage.getToken();
    const username = storage.getUsername();
    set({
      token,
      username,
      isAuthenticated: !!token,
    });
  },

  clearError: () => {
    set({ error: null });
  },
};
});
