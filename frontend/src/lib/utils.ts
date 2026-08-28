import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { format, formatDistanceToNow, isToday, isTomorrow, isYesterday, parseISO } from 'date-fns';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatCurrency(amount: number | undefined | null, currency: string = 'INR'): string {
  if (amount === undefined || amount === null || isNaN(amount)) {
    return '0';
  }

  const symbolMap: Record<string, string> = {
    INR: '₹',
    USD: '$',
    EUR: '€',
    GBP: '£',
    JPY: '¥',
    CAD: 'CA$',
    AUD: 'A$',
  };

  const symbol = symbolMap[currency.toUpperCase()] || currency;
  const formattedNumber = new Intl.NumberFormat(currency === 'INR' ? 'en-IN' : 'en-US', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0,
  }).format(amount);

  return `${symbol}${formattedNumber}`;
}

export function formatDate(dateString: string | undefined | null, formatStr: string = 'MMM dd, yyyy'): string {
  if (!dateString) return '';
  try {
    const date = parseISO(dateString);
    return format(date, formatStr);
  } catch (err) {
    return dateString;
  }
}

export function formatRelativeDate(dateString: string | undefined | null): string {
  if (!dateString) return '';
  try {
    const date = parseISO(dateString);
    if (isToday(date)) return 'Today';
    if (isTomorrow(date)) return 'Tomorrow';
    if (isYesterday(date)) return 'Yesterday';
    return format(date, 'MMM dd');
  } catch (err) {
    return dateString;
  }
}

export function formatTimeAgo(dateString: string | undefined | null): string {
  if (!dateString) return '';
  try {
    const date = parseISO(dateString);
    return formatDistanceToNow(date, { addSuffix: true });
  } catch (err) {
    return dateString;
  }
}
