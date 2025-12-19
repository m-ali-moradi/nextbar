import { defineStore } from 'pinia';
import { authApi } from '../api';

interface AuthState {
  user: any | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: null,
    loading: false,
    error: null,
  }),
  actions: {
    async login(username: string, password: string) {
      this.loading = true;
      try {
        const response = await authApi.login(username, password);
        // Support APIs that return different token shapes (token | accessToken)
        const token = response.data?.token ?? response.data?.accessToken ?? response.data?.access_token;
        const refreshToken = response.data?.refreshToken ?? response.data?.refresh_token;

        // Set user from response if provided, otherwise try to decode from JWT
        if (response.data?.user) {
          this.user = response.data.user;
        } else if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.user = { username: payload.sub, roles: payload.roles ?? [] };
          } catch {
            this.user = null;
          }
        } else {
          this.user = null;
        }

        this.token = token ?? null;
        if (this.token) localStorage.setItem('authToken', this.token);
        if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
        this.error = null;
      } catch (error) {
        this.error = (error as Error).message;
      } finally {
        this.loading = false;
      }
    },
    async register(username: string, password: string, email: string) {
      this.loading = true;
      try {
        const response = await authApi.register(username, password, email);
        const token = response.data?.token ?? response.data?.accessToken ?? response.data?.access_token;
        const refreshToken = response.data?.refreshToken ?? response.data?.refresh_token;

        if (response.data?.user) {
          this.user = response.data.user;
        } else if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.user = { username: payload.sub, roles: payload.roles ?? [] };
          } catch {
            this.user = null;
          }
        } else {
          this.user = null;
        }

        this.token = token ?? null;
        if (this.token) localStorage.setItem('authToken', this.token);
        if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
        this.error = null;
      } catch (error) {
        this.error = (error as Error).message;
      } finally {
        this.loading = false;
      }
    },
    logout() {
      this.user = null;
      this.token = null;
      localStorage.removeItem('authToken');
    },
  },
  persist: true,
});