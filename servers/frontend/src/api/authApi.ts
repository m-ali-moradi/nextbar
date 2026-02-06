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
    return ApiService.get('/api/users');
  },
  createUser(data: any): Promise<AxiosResponse> {
    return ApiService.post('/api/users', data);
  },
  updateUser(userId: string, data: any): Promise<AxiosResponse> {
    return ApiService.put(`/api/users/${userId}`, data);
  },
  deleteUser(userId: string): Promise<AxiosResponse> {
    return ApiService.delete(`/api/users/${userId}`);
  },
};