import { defineStore } from 'pinia';
import { ref } from 'vue';
import { authApi } from '@/api';

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
  firstName?: string;
  lastName?: string;
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

  const rawRoles = Array.isArray(input.roles)
    ? input.roles
    : Array.isArray(input.assignments)
      ? input.assignments
      : [];

  const roles: UserRole[] = rawRoles
    .map((r: any) => ({
      service: normalizeServiceCode(r?.service ?? r?.serviceCode),
      role: normalizeRoleName(r?.role ?? r?.roleName),
      resourceId: r?.resourceId ?? null,
    }))
    .filter((r) => !!r.service && !!r.role);

  const isAdmin = roles.some((r) => r?.role === 'ADMIN');

  return {
    id: input.id,
    username: input.username,
    email: input.email,
    firstName: input.firstName,
    lastName: input.lastName,
    roles,
    isAdmin,
  };
}

function hasServiceCheck(user: NormalizedUser | null, service: ServiceCode, requiredRole?: RoleName): boolean {
  if (!user) return false;
  if (user.isAdmin) return true;
  return user.roles.some((r) =>
    r?.service === service && (requiredRole ? r?.role === requiredRole : true)
  );
}

export function defaultRouteForUser(user: NormalizedUser | null): string {
  if (!user) return '/login';
  if (user.isAdmin) return '/admin/users';

  if (hasServiceCheck(user, 'BAR')) return '/bars';
  if (hasServiceCheck(user, 'EVENT', 'MANAGER')) return '/events';
  if (hasServiceCheck(user, 'DROP_POINT')) return '/droppoints';
  if (hasServiceCheck(user, 'WAREHOUSE')) return '/warehouse';
  return '/login';
}

function decodeJwtPayload(token: string): any | null {
  try {
    const payloadPart = token.split('.')[1];
    if (!payloadPart) return null;
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
    if (typeof item === 'string') {
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
      continue;
    }

    if (item && typeof item === 'object') {
      const service = normalizeServiceCode((item as any).service ?? (item as any).serviceCode);
      const role = normalizeRoleName((item as any).role ?? (item as any).roleName);
      const resourceId = (item as any).resourceId ?? null;
      if (!service || !role) continue;
      parsed.push({ service, role, resourceId });
    }
  }

  return parsed;
}

/** Extract user + roles from a JWT token response */
function userFromTokenResponse(responseData: any): { user: NormalizedUser | null; token: string | null } {
  const token = responseData?.token ?? responseData?.accessToken ?? responseData?.access_token;
  let user: NormalizedUser | null = null;

  if (responseData?.user) {
    user = normalizeUser(responseData.user);
  } else if (token) {
    const payload = decodeJwtPayload(token);
    if (payload) {
      const assignmentRoles = parseAssignments(payload.assignments);
      const legacyRoles = Array.isArray(payload.roles)
        ? payload.roles.map((role: any) => ({ role }))
        : [];
      const roles = assignmentRoles.length > 0 ? assignmentRoles : legacyRoles;
      const emailFromPayload = payload.email ?? payload.preferred_username ?? payload.username ?? null;
      user = normalizeUser({ username: payload.sub, email: emailFromPayload, roles });
    }
  }

  return { user, token: token ?? null };
}

export const useAuthStore = defineStore('auth', () => {
  // ============ State ============
  const user = ref<NormalizedUser | null>(null);
  const token = ref<string | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // ============ Actions ============
  async function login(username: string, password: string) {
    loading.value = true;
    error.value = null;
    try {
      const response = await authApi.login(username, password);
      const result = userFromTokenResponse(response.data);
      user.value = result.user;
      token.value = result.token;
      // Keep token only in-memory; allow short-lived session fallback
      if (token.value) {
        try {
          sessionStorage.setItem('authToken', token.value);
        } catch {
        }
      }
      if (token.value) {
        const { useWebSocketEvents } = await import('@/composables/useWebSocketEvents');
        useWebSocketEvents().connect();
      }
      error.value = null;
    } catch (err) {
      error.value = (err as Error).message;
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    // Call logout endpoint without sending refresh token in body (prepare for cookie-based refresh)
    try {
      await authApi.logout();
    } catch {
      // Ignore remote logout failures and clear local session anyway.
    }
    user.value = null;
    token.value = null;
    const { useWebSocketEvents } = await import('@/composables/useWebSocketEvents');
    useWebSocketEvents().disconnect();
    try {
      sessionStorage.removeItem('authToken');
    } catch {
    }
  }

  async function refreshSession() {
    // Refresh flow will be migrated to cookie-based handling. For now, attempt a refresh
    // without sending a client-held refresh token (backend should accept cookie-based refresh).
    try {
      const response = await authApi.refresh();
      const result = userFromTokenResponse(response.data);
      if (result.token) {
        token.value = result.token;
        try { sessionStorage.setItem('authToken', result.token); } catch {}
      }
      if (result.user) {
        user.value = result.user;
      } else {
        syncUserFromToken();
      }
    } catch {
      // No-op: resource-sync updates are eventually visible on next login/refresh.
    }
  }

  function getDefaultRoute(): string {
    return defaultRouteForUser(user.value);
  }

  function syncUserFromToken() {
    const t = token.value || sessionStorage.getItem('authToken');
    if (!t) {
      user.value = null;
      token.value = null;
      error.value = null;
      return;
    }

    const payload = decodeJwtPayload(t);
    if (!payload) {
      user.value = null;
      token.value = null;
      error.value = null;
      try { sessionStorage.removeItem('authToken'); } catch {}
      return;
    }

    const assignmentRoles = parseAssignments(payload.assignments);
    const legacyRoles = Array.isArray(payload.roles)
      ? payload.roles.map((role: any) => ({ role }))
      : [];
    const roles = assignmentRoles.length > 0 ? assignmentRoles : legacyRoles;
    const emailFromPayload = payload.email ?? payload.preferred_username ?? payload.username ?? null;

    user.value = normalizeUser({
      id: payload.userId,
      username: payload.sub,
      email: emailFromPayload,
      roles,
    });
    token.value = t;
  }

  return {
    // State
    user,
    token,
    loading,
    error,
    // Actions
    login,
    logout,
    refreshSession,
    getDefaultRoute,
    syncUserFromToken,
  };
  }, {
  persist: {
    // Do NOT persist tokens to long-lived storage. Persist only the user profile if desired.
    pick: ['user'],
  },
});