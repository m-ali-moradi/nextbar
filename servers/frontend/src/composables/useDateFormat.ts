/**
 * Shared date formatting helpers.
 * Eliminates duplicated formatDate / fmtDate implementations across views.
 */

/** Short date: "Wed, Feb 16, 2026" */
export function formatDate(date: string | Date | null | undefined): string {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

/** Long date: "Wednesday, February 16, 2026" */
export function formatDateLong(date: string | Date | null | undefined): string {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

/** Simple locale date with em-dash fallback. */
export function formatDateShort(date: string | Date | null | undefined): string {
  return date ? new Date(date).toLocaleDateString() : '—';
}

/** Relative time: "Just now", "5 min ago", "2h ago", "3d ago" */
export function formatRelativeTime(date: string | Date | null | undefined): string {
  if (!date) return '—';
  const updated = new Date(date);
  const updatedTime = updated.getTime();
  if (!Number.isFinite(updatedTime)) return '—';

  const diffMs = Math.max(0, Date.now() - updatedTime);
  const diffSeconds = Math.floor(diffMs / 1000);
  if (diffSeconds < 60) return 'Just now';

  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `${diffMinutes} min ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}h ago`;

  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays}d ago`;
}
