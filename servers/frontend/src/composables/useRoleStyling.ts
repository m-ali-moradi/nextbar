/**
 * Shared role & service styling helpers.
 * Eliminates duplicated role badge, avatar, icon, and service icon maps
 * from ProfileView, ManageAccounts, EditUserModal, CreateUser, etc.
 */

// ───── Roles ─────

export const availableRoles = [
  { value: 'ADMIN', label: 'Admin', icon: 'fas fa-user-shield text-violet-500' },
  { value: 'MANAGER', label: 'Manager', icon: 'fas fa-user-tie text-blue-500' },
  { value: 'OPERATOR', label: 'Operator', icon: 'fas fa-user text-emerald-500' },
] as const;

// ───── Services ─────

export const availableServices = [
  { value: 'BAR', label: 'Bar', icon: 'fas fa-glass-martini text-bar-500' },
  { value: 'EVENT', label: 'Event', icon: 'fas fa-calendar text-event-500' },
  { value: 'DROP_POINT', label: 'Drop Point', icon: 'fas fa-map-marker-alt text-droppoint-500' },
  { value: 'WAREHOUSE', label: 'Warehouse', icon: 'fas fa-warehouse text-warehouse-500' },
] as const;

// ───── Styling Maps ─────

const ROLE_BADGE: Record<string, string> = {
  ADMIN: 'bg-violet-100 text-violet-700',
  MANAGER: 'bg-blue-100 text-blue-700',
  OPERATOR: 'bg-emerald-100 text-emerald-700',
};

const ROLE_BADGE_WITH_PREFIX: Record<string, string> = {
  ADMIN: 'badge badge-info bg-violet-100 text-violet-700',
  MANAGER: 'badge badge-info bg-blue-100 text-blue-700',
  OPERATOR: 'badge badge-info bg-emerald-100 text-emerald-700',
};

const AVATAR_GRADIENT: Record<string, string> = {
  ADMIN: 'bg-gradient-to-br from-violet-500 to-violet-600',
  MANAGER: 'bg-gradient-to-br from-blue-500 to-blue-600',
  OPERATOR: 'bg-gradient-to-br from-emerald-500 to-emerald-600',
};

const ROLE_ICON: Record<string, string> = {
  ADMIN: 'fas fa-user-shield',
  MANAGER: 'fas fa-user-tie',
  OPERATOR: 'fas fa-user',
};

const SERVICE_ICON: Record<string, string> = {
  ALL: 'fas fa-globe text-violet-500',
  BAR: 'fas fa-glass-martini text-bar-500',
  DROP_POINT: 'fas fa-map-marker-alt text-droppoint-500',
  WAREHOUSE: 'fas fa-warehouse text-warehouse-500',
  EVENT: 'fas fa-calendar text-event-500',
};

// ───── Functions ─────

/** Role badge CSS classes (light variant without badge prefix). */
export function getRoleBadgeClass(role: string): string {
  return ROLE_BADGE[role] || 'badge-neutral';
}

/** Role badge CSS classes with `badge badge-info` prefix for table rows. */
export function getRoleBadgeClassFull(role: string): string {
  return ROLE_BADGE_WITH_PREFIX[role] || 'badge badge-neutral';
}

/** Avatar gradient classes. */
export function getAvatarClass(role: string): string {
  return AVATAR_GRADIENT[role] || 'bg-gradient-to-br from-slate-400 to-slate-500';
}

/** FontAwesome icon class for a role. */
export function getRoleIcon(role: string): string {
  return ROLE_ICON[role] || 'fas fa-user';
}

/** FontAwesome icon class (with color) for a service. */
export function getServiceIcon(service: string): string {
  return SERVICE_ICON[service] || 'fas fa-circle text-slate-400';
}

/** First letter of a name, uppercased. */
export function getInitials(name: string | null | undefined): string {
  if (!name) return '?';
  return name.charAt(0).toUpperCase();
}

/** Normalize a raw role name to canonical form. */
export function normalizeRole(name: string | null | undefined): string {
  if (!name) return '';
  const r = String(name).toUpperCase();
  if (r === 'ADMIN') return 'ADMIN';
  if (r.includes('MANAGER')) return 'MANAGER';
  if (r.includes('BARTENDER') || r.includes('OPERATOR')) return 'OPERATOR';
  return r;
}

/** Strip `_SERVICE` suffix from a raw service code. */
export function normalizeService(code: string | null | undefined): string {
  if (!code) return '';
  const s = String(code).toUpperCase();
  return s.endsWith('_SERVICE') ? s.slice(0, -'_SERVICE'.length) : s;
}
