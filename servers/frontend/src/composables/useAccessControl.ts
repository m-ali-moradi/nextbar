/**
 * Shared access-control helpers used by the router guard and the Sidebar.
 *
 * The "user" objects here match the NormalizedUser shape produced by authStore.
 */

type ServiceCode = string;
type RoleName = string;

interface UserRole {
  service?: ServiceCode | null;
  role?: RoleName | null;
  resourceId?: string | null;
}

interface NormalizedUser {
  id?: string;
  username?: string;
  email?: string;
  roles: UserRole[];
  isAdmin: boolean;
}

export function isAdmin(user: NormalizedUser | null | undefined): boolean {
  return !!user?.isAdmin;
}

export function hasService(user: NormalizedUser | null | undefined, serviceCode: ServiceCode): boolean {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return Array.isArray(user.roles) && user.roles.some((r) => r?.service === serviceCode);
}

export function hasManagerRole(user: NormalizedUser | null | undefined, serviceCode: ServiceCode): boolean {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return (
    Array.isArray(user.roles) &&
    user.roles.some((r) => r?.service === serviceCode && r?.role === 'MANAGER')
  );
}

export function hasAnyManagerRole(user: NormalizedUser | null | undefined, serviceCodes: ServiceCode[]): boolean {
  if (!user) return false;
  if (isAdmin(user)) return true;
  if (!Array.isArray(serviceCodes) || serviceCodes.length === 0) return false;
  return serviceCodes.some((sc) => hasManagerRole(user, sc));
}

export function canAccessDroppoints(user: NormalizedUser | null | undefined): boolean {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return hasService(user, 'DROP_POINT');
}

export function hasResourceAccess(
  user: NormalizedUser | null | undefined,
  serviceCode: ServiceCode,
  resourceId: string | string[] | undefined,
): boolean {
  if (!user) return false;
  if (isAdmin(user)) return true;
  if (!Array.isArray(user.roles)) return false;

  // MANAGER => all resources for that service.
  if (user.roles.some((r) => r?.service === serviceCode && r?.role === 'MANAGER')) return true;

  // OPERATOR => only assigned resource.
  const rid = Array.isArray(resourceId) ? resourceId[0] : resourceId;
  return user.roles.some(
    (r) => r?.service === serviceCode && r?.role === 'OPERATOR' && String(r?.resourceId) === String(rid),
  );
}
