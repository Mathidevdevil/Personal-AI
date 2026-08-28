import React from 'react';
import { cn } from '../../lib/utils';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: {
    value: string;
    isPositive?: boolean;
    label?: string;
  };
  glowColor?: 'indigo' | 'emerald' | 'rose' | 'amber' | 'cyan';
  className?: string;
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  trend,
  glowColor = 'indigo',
  className,
}) => {
  const glowStyles = {
    indigo: 'from-indigo-500/10 to-transparent border-brand-500/20 text-brand-600',
    emerald: 'from-emerald-500/10 to-transparent border-emerald-500/20 text-emerald-400',
    rose: 'from-rose-500/10 to-transparent border-rose-500/20 text-rose-400',
    amber: 'from-amber-500/10 to-transparent border-amber-500/20 text-amber-400',
    cyan: 'from-cyan-500/10 to-transparent border-cyan-500/20 text-cyan-400',
  };

  const iconBgStyles = {
    indigo: 'bg-brand-600/15 text-brand-600 border border-brand-500/30',
    emerald: 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30',
    rose: 'bg-rose-500/15 text-rose-400 border border-rose-500/30',
    amber: 'bg-amber-500/15 text-amber-400 border border-amber-500/30',
    cyan: 'bg-cyan-500/15 text-cyan-400 border border-cyan-500/30',
  };

  return (
    <div
      className={cn(
        'relative overflow-hidden rounded-2xl bg-gradient-to-b p-6 glass-card glass-card-hover border transition-all duration-300',
        glowStyles[glowColor],
        className
      )}
    >
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <p className="text-xs font-semibold uppercase tracking-wider text-muted">{title}</p>
          <div className="text-2xl lg:text-3xl font-bold tracking-tight text-foreground">{value}</div>
          {subtitle && <p className="text-xs text-muted">{subtitle}</p>}
        </div>
        <div className={cn('p-3 rounded-xl shrink-0', iconBgStyles[glowColor])}>
          {icon}
        </div>
      </div>

      {trend && (
        <div className="mt-4 flex items-center gap-1.5 text-xs">
          <span
            className={cn(
              'font-semibold px-2 py-0.5 rounded-md flex items-center gap-0.5',
              trend.isPositive
                ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                : 'bg-rose-500/15 text-rose-400 border border-rose-500/30'
            )}
          >
            {trend.isPositive ? '↑' : '↓'} {trend.value}
          </span>
          {trend.label && <span className="text-muted">{trend.label}</span>}
        </div>
      )}
    </div>
  );
};
