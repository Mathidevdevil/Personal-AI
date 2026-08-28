import React, { useEffect, useState } from 'react';
import { X, Bell, Check, CheckCheck, AlertCircle, Calendar, DollarSign, Clock } from 'lucide-react';
import { Notification } from '../../types';
import { notificationApi } from '../../lib/api';
import { formatTimeAgo, cn } from '../../lib/utils';
import { Button } from '../../components/common/Button';

interface NotificationsDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  onNotificationRead?: () => void;
}

export const NotificationsDrawer: React.FC<NotificationsDrawerProps> = ({
  isOpen,
  onClose,
  onNotificationRead,
}) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  const fetchNotifications = async () => {
    setIsLoading(true);
    try {
      const data = await notificationApi.getNotifications(filter === 'unread');
      setNotifications(data);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen, filter]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      if (onNotificationRead) onNotificationRead();
    } catch (err) {
      console.error('Failed to mark read:', err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      if (onNotificationRead) onNotificationRead();
    } catch (err) {
      console.error('Failed to mark all read:', err);
    }
  };

  if (!isOpen) return null;

  const getIcon = (type: string) => {
    switch (type) {
      case 'TASK_DUE':
      case 'TASK_OVERDUE':
        return <Clock className="w-4 h-4 text-amber-400" />;
      case 'BUDGET_WARNING':
        return <DollarSign className="w-4 h-4 text-rose-400" />;
      case 'TRIP_REMINDER':
        return <Calendar className="w-4 h-4 text-brand-600" />;
      default:
        return <AlertCircle className="w-4 h-4 text-sky-400" />;
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-background/60 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-[100vw] max-w-md glass-card border-l border-border shadow-2xl flex flex-col">
          {/* Header */}
          <div className="p-4 sm:p-6 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-xl bg-brand-600/15 text-brand-600 border border-brand-500/30">
                <Bell className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-foreground">Notifications</h3>
                <p className="text-xs text-muted">Activity and system alerts</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-muted hover:text-foreground hover:bg-surface-hover"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Action & Filter Bar */}
          <div className="px-6 py-3 border-b border-border/80 flex items-center justify-between bg-surface/40 text-xs">
            <div className="flex gap-2">
              <button
                onClick={() => setFilter('all')}
                className={cn(
                  'px-2.5 py-1 rounded-lg transition-colors',
                  filter === 'all'
                    ? 'bg-brand-600 text-foreground font-medium'
                    : 'text-muted hover:text-foreground'
                )}
              >
                All
              </button>
              <button
                onClick={() => setFilter('unread')}
                className={cn(
                  'px-2.5 py-1 rounded-lg transition-colors',
                  filter === 'unread'
                    ? 'bg-brand-600 text-foreground font-medium'
                    : 'text-muted hover:text-foreground'
                )}
              >
                Unread
              </button>
            </div>

            <button
              onClick={handleMarkAllRead}
              className="text-muted hover:text-brand-600 flex items-center gap-1 transition-colors"
            >
              <CheckCheck className="w-3.5 h-3.5" />
              Mark all read
            </button>
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {isLoading ? (
              <div className="p-8 text-center text-muted text-sm">Loading alerts...</div>
            ) : notifications.length === 0 ? (
              <div className="p-8 text-center text-muted text-sm">
                No notifications right now!
              </div>
            ) : (
              notifications.map((notif) => (
                <div
                  key={notif.id}
                  className={cn(
                    'p-4 rounded-xl border transition-all text-left flex gap-3',
                    notif.isRead
                      ? 'bg-surface/40 border-border/60 opacity-70'
                      : 'bg-surface-hover/60 border-brand-500/30'
                  )}
                >
                  <div className="mt-0.5 shrink-0">{getIcon(notif.type)}</div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <h4 className="text-sm font-semibold text-foreground truncate">{notif.title}</h4>
                      <span className="text-[10px] text-muted whitespace-nowrap">
                        {formatTimeAgo(notif.createdAt)}
                      </span>
                    </div>
                    <p className="text-xs text-foreground mt-1 leading-relaxed">{notif.message}</p>
                    {!notif.isRead && (
                      <button
                        onClick={() => handleMarkAsRead(notif.id)}
                        className="mt-2 text-[11px] text-brand-600 hover:text-indigo-300 flex items-center gap-1 font-medium"
                      >
                        <Check className="w-3 h-3" /> Mark as read
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
