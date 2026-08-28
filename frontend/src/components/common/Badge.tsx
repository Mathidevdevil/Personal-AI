import React from 'react';
import { cn } from '../../lib/utils';
import { TaskStatus, TaskPriority, TripStatus, ExpenseCategory } from '../../types';

interface BadgeProps {
  children?: React.ReactNode;
  variant?:
    | 'default'
    | 'success'
    | 'warning'
    | 'danger'
    | 'info'
    | 'purple'
    | 'indigo'
    | 'cyan'
    | 'gray';
  status?: TaskStatus | TripStatus;
  priority?: TaskPriority;
  category?: ExpenseCategory;
  className?: string;
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'default',
  status,
  priority,
  category,
  className,
  size = 'md',
}) => {
  let computedVariant = variant;
  let text = children;

  if (status) {
    switch (status) {
      case 'COMPLETED':
        computedVariant = 'success';
        text = text || 'Completed';
        break;
      case 'IN_PROGRESS':
      case 'ACTIVE':
        computedVariant = 'indigo';
        text = text || (status === 'ACTIVE' ? 'Active' : 'In Progress');
        break;
      case 'TODO':
      case 'PLANNED':
        computedVariant = 'cyan';
        text = text || (status === 'PLANNED' ? 'Planned' : 'To Do');
        break;
      case 'CANCELLED':
        computedVariant = 'gray';
        text = text || 'Cancelled';
        break;
    }
  } else if (priority) {
    switch (priority) {
      case 'URGENT':
        computedVariant = 'danger';
        text = text || 'Urgent';
        break;
      case 'HIGH':
        computedVariant = 'warning';
        text = text || 'High';
        break;
      case 'MEDIUM':
        computedVariant = 'indigo';
        text = text || 'Medium';
        break;
      case 'LOW':
        computedVariant = 'gray';
        text = text || 'Low';
        break;
    }
  } else if (category) {
    text = text || category;
    switch (category) {
      case 'FOOD':
        computedVariant = 'warning';
        break;
      case 'SHOPPING':
        computedVariant = 'purple';
        break;
      case 'TRANSPORT':
      case 'TRAVEL':
        computedVariant = 'cyan';
        break;
      case 'BILLS':
      case 'HEALTH':
        computedVariant = 'danger';
        break;
      case 'EDUCATION':
        computedVariant = 'indigo';
        break;
      case 'ENTERTAINMENT':
        computedVariant = 'success';
        break;
      default:
        computedVariant = 'gray';
    }
  }

  const variantStyles = {
    default: 'bg-surface-hover text-foreground border-hover',
    success: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
    warning: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
    danger: 'bg-rose-500/15 text-rose-400 border-rose-500/30',
    info: 'bg-sky-500/15 text-sky-400 border-sky-500/30',
    indigo: 'bg-brand-600/15 text-brand-600 border-brand-500/30',
    purple: 'bg-purple-500/15 text-purple-400 border-purple-500/30',
    cyan: 'bg-teal-500/15 text-teal-400 border-teal-500/30',
    gray: 'bg-slate-200/40 text-muted border-slate-600/40',
  };

  const sizeStyles = {
    sm: 'text-[10px] px-2 py-0.5 font-semibold',
    md: 'text-xs px-2.5 py-1 font-medium',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-lg border uppercase tracking-wider',
        variantStyles[computedVariant],
        sizeStyles[size],
        className
      )}
    >
      {text}
    </span>
  );
};
