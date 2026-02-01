/**
 * Parse a date string consistently, handling LocalDateTime format from Java backend
 * Java LocalDateTime comes without timezone (e.g., "2026-01-30T10:30:00")
 * We treat it as local time for consistency
 */
function parseDate(dateString: string): Date {
  // If the string doesn't have timezone info, treat it as local time
  // by replacing 'T' with space which forces local time parsing
  if (!dateString.includes('Z') && !dateString.includes('+') && !dateString.match(/[+-]\d{2}:\d{2}$/)) {
    return new Date(dateString.replace('T', ' '));
  }
  return new Date(dateString);
}

/**
 * Format a date string to a human-readable "time ago" format
 */
export function formatDistanceToNow(dateString: string): string {
  const date = parseDate(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return 'just now';
  }

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes}m ago`;
  }

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours}h ago`;
  }

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays}d ago`;
  }

  const diffInWeeks = Math.floor(diffInDays / 7);
  if (diffInWeeks < 4) {
    return `${diffInWeeks}w ago`;
  }

  const diffInMonths = Math.floor(diffInDays / 30);
  if (diffInMonths < 12) {
    return `${diffInMonths}mo ago`;
  }

  const diffInYears = Math.floor(diffInDays / 365);
  return `${diffInYears}y ago`;
}

/**
 * Format a date to a localized string
 */
export function formatDate(dateString: string, options?: Intl.DateTimeFormatOptions): string {
  const date = parseDate(dateString);
  return date.toLocaleDateString(undefined, options ?? {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

/**
 * Format a date to include time
 */
export function formatDateTime(dateString: string): string {
  const date = parseDate(dateString);
  return date.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
