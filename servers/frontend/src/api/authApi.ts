import { AxiosResponse } from 'axios';
import ApiService from './index';

export const authApi = {
  login(username: string, password: string): Promise<AxiosResponse> {
    return ApiService.post('/api/auth/login', { username, password });
  },
  register(username: string, password: string, email: string): Promise<AxiosResponse> {
    return ApiService.post('/api/auth/register', { username, password, email });
  },
  getUsers(): Promise<AxiosResponse> {
    return ApiService.get('/api/auth/users');
  },
  updateUser(userId: string, data: any): Promise<AxiosResponse> {
    return ApiService.put(`/api/auth/users/${userId}`, data);
  },
  deleteUser(userId: string): Promise<AxiosResponse> {
    return ApiService.delete(`/api/auth/users/${userId}`);
  },
};