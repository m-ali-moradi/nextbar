import { defineStore } from 'pinia';
import { authApi } from '../api';

interface AuthState {
  user: any | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

type ServiceCode = 'BAR' | 'DROP_POINT' | 'WAREHOUSE' | 'EVENT' | string;
type RoleName = 'ADMIN' | 'MANAGER' | 'OPERATOR' | string;

type UserRole = {
  service?: ServiceCode | null;
  role?: RoleName | null;
  resourceId?: string | null;
};

type NormalizedUser = {
  id?: string;
  username?: string;
  email?: string;
  roles: UserRole[];
  isAdmin: boolean;
};

function normalizeServiceCode(raw: unknown): ServiceCode | null {
  if (raw == null) return null;
  const s = String(raw).trim().toUpperCase();
  if (!s) return null;

  let normalized = s;
  if (normalized.endsWith('_SERVICE')) normalized = normalized.slice(0, -'_SERVICE'.length);
  if (normalized.endsWith('-SERVICE')) normalized = normalized.slice(0, -'-SERVICE'.length);

  if (normalized === 'DROPPOINT' || normalized === 'DROPPOINTS') return 'DROP_POINT';
  if (normalized === 'DROP_POINT' || normalized === 'DROPPOINT_SERVICE') return 'DROP_POINT';
  return normalized;
}

function normalizeRoleName(raw: unknown): RoleName | null {
  if (raw == null) return null;
  const r = String(raw).trim().toUpperCase();
  if (!r) return null;
  if (r === 'ADMIN') return 'ADMIN';
  if (r.includes('MANAGER')) return 'MANAGER';
  if (r.includes('BARTENDER') || r.includes('OPERATOR')) return 'OPERATOR';
  return r;
}

function normalizeUser(input: any | null): NormalizedUser | null {
  if (!input) return null;

  const roles: UserRole[] = Array.isArray(input.roles)
    ? input.roles.map((r: any) => ({
        service: normalizeServiceCode(r?.service ?? r?.serviceCode),
        role: normalizeRoleName(r?.role ?? r?.roleName),
        resourceId: r?.resourceId ?? null,
      }))
    : [];

  const isAdmin = roles.some((r) => r?.role === 'ADMIN');

  return {
    id: input.id,
    username: input.username,
    email: input.email,
    roles,
    isAdmin,
  };
}

function hasService(user: NormalizedUser | null, service: ServiceCode): boolean {
  if (!user) return false;
  if (user.isAdmin) return true;
  return user.roles.some((r) => r?.service === service);
}

export function defaultRouteForUser(user: NormalizedUser | null): string {
  if (!user) return '/login';
  if (user.isAdmin) return '/bars';

  // Prefer landing in an area the user can access.
  if (hasService(user, 'BAR')) return '/bars';
  if (hasService(user, 'EVENT')) return '/events';
  if (hasService(user, 'DROP_POINT')) return '/droppoints';
  if (hasService(user, 'WAREHOUSE')) return '/warehouse';
  // If the user has no service roles we can route to, avoid redirect loops.
  return '/login';
}

function decodeJwtPayload(token: string): any | null {
  try {
    const payloadPart = token.split('.')[1];
    if (!payloadPart) return null;
    // base64url -> base64
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    return JSON.parse(atob(padded));
  } catch {
    return null;
  }
}

function parseAssignments(assignments: unknown): UserRole[] {
  if (!Array.isArray(assignments)) return [];

  const parsed: UserRole[] = [];
  for (const item of assignments) {
    if (typeof item !== 'string') continue;
    const [serviceRaw, roleRaw, resourceRaw] = item.split(':');
    const service = normalizeServiceCode(serviceRaw);
    const role = normalizeRoleName(roleRaw);
    const resourceIdRaw = (resourceRaw ?? '').trim();

    if (!service || !role) continue;

    parsed.push({
      service,
      role,
      resourceId: !resourceIdRaw || resourceIdRaw === '*' ? null : resourceIdRaw,
    });
  }

  return parsed;
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
          this.user = normalizeUser(response.data.user);
        } else if (token) {
          const payload = decodeJwtPayload(token);
          if (payload) {
            const assignmentRoles = parseAssignments(payload.assignments);
            const legacyRoles = Array.isArray(payload.roles)
              ? payload.roles.map((role: any) => ({ role }))
              : [];
            const roles = assignmentRoles.length > 0 ? assignmentRoles : legacyRoles;
            this.user = normalizeUser({ username: payload.sub, roles });
          } else {
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
          this.user = normalizeUser(response.data.user);
        } else if (token) {
          const payload = decodeJwtPayload(token);
          if (payload) {
            const assignmentRoles = parseAssignments(payload.assignments);
            const legacyRoles = Array.isArray(payload.roles)
              ? payload.roles.map((role: any) => ({ role }))
              : [];
            const roles = assignmentRoles.length > 0 ? assignmentRoles : legacyRoles;
            this.user = normalizeUser({ username: payload.sub, roles });
          } else {
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

    getDefaultRoute(): string {
      return defaultRouteForUser(this.user);
    },
  },
  persist: true,
});