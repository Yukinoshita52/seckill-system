import apiClient from './index';
import { LoginRequest, RegisterRequest, LoginResponse } from '../types/user';
import { ApiResponse } from '../types/api';

export const authApi = {
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return apiClient.post('/auth/login', data);
  },

  register(data: RegisterRequest): Promise<ApiResponse<void>> {
    return apiClient.post('/auth/register', data);
  },
};
