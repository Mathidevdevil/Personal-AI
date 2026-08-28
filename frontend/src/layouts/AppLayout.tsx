import React, { useState, useEffect } from 'react';
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Wallet,
  CheckSquare,
  Plane,
  Sparkles,
  Settings,
  LogOut,
  Bell,
  Search,
  Menu,
  X,
  Send,
  User as UserIcon,
  Sun,
  Moon,
} from 'lucide-react';
import { useAuthStore } from '../stores/authStore';
import { useThemeStore } from '../stores/themeStore';
import { notificationApi } from '../lib/api';
import { NotificationsDrawer } from '../features/notifications/NotificationsDrawer';
import { cn } from '../lib/utils';

export const AppLayout: React.FC = () => {
  const { user, logout } = useAuthStore();
  const { theme, toggleTheme } = useThemeStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [quickPrompt, setQuickPrompt] = useState('');

  const fetchUnreadCount = async () => {
    try {
      const notifs = await notificationApi.getNotifications(true);
      setUnreadCount(notifs.length);
    } catch (err) {
      console.error('Failed to fetch unread count:', err);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Finance', path: '/finance', icon: Wallet },
    { name: 'Tasks', path: '/tasks', icon: CheckSquare },
    { name: 'Travel', path: '/travel', icon: Plane },
    { name: 'AI Assistant', path: '/assistant', icon: Sparkles, highlight: true },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  const handleQuickPromptSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!quickPrompt.trim()) return;
    navigate('/assistant', { state: { initialPrompt: quickPrompt.trim() } });
    setQuickPrompt('');
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col md:flex-row">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex flex-col w-64 border-r border-border/80 bg-background/80 backdrop-blur-xl shrink-0 p-5 justify-between fixed top-0 bottom-0 z-30">
        <div>
          {/* Brand Logo */}
          <div className="flex items-center gap-3 px-2 py-2 mb-8 cursor-pointer" onClick={() => navigate('/')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-pink-500 p-0.5 shadow-lg shadow-indigo-500/25 flex items-center justify-center">
              <div className="w-full h-full bg-background rounded-[10px] flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-brand-600" />
              </div>
            </div>
            <div>
              <h1 className="font-bold text-base tracking-tight text-foreground flex items-center gap-1.5">
                Personal AI <span className="text-[10px] px-1.5 py-0.5 bg-brand-600/20 text-brand-600 rounded-md border border-brand-500/30">Pro</span>
              </h1>
              <p className="text-[11px] text-muted">Intelligent Life OS</p>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="space-y-1.5">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={cn(
                    'flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 group relative',
                    isActive
                      ? 'bg-brand-600/15 text-brand-600 border border-brand-500/30 shadow-sm'
                      : 'text-muted hover:text-foreground hover:bg-surface/60'
                  )}
                >
                  <Icon
                    className={cn(
                      'w-5 h-5 transition-transform group-hover:scale-110 shrink-0',
                      isActive ? 'text-brand-600' : 'text-muted group-hover:text-foreground',
                      item.highlight && !isActive && 'text-pink-600'
                    )}
                  />
                  <span>{item.name}</span>
                  {item.highlight && !isActive && (
                    <span className="ml-auto text-[10px] font-semibold px-1.5 py-0.5 rounded bg-pink-500/15 text-pink-600 border border-pink-400/30">
                      AI
                    </span>
                  )}
                  {isActive && (
                    <div className="absolute right-0 w-1.5 h-6 bg-brand-600 rounded-l-full" />
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>
        {/* Theme Toggle & User Card */}
        <div className="pt-4 border-t border-border/80 flex flex-col gap-2">
          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium text-muted hover:text-foreground hover:bg-surface/60 transition-colors w-full text-left"
          >
            {theme === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
            <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>
          </button>

          {/* User Card */}
          <div className="flex items-center justify-between p-2 rounded-xl bg-surface/40 border border-border/60">
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="w-8 h-8 rounded-lg bg-brand-600/20 text-brand-600 flex items-center justify-center font-bold text-xs shrink-0 border border-brand-500/30">
                {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
              </div>
              <div className="truncate">
                <p className="text-xs font-semibold text-foreground truncate">{user?.name || 'User'}</p>
                <p className="text-[10px] text-muted truncate">{user?.email || 'user@example.com'}</p>
              </div>
            </div>
            <button
              onClick={handleLogout}
              title="Log out"
              className="p-1.5 text-muted hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col md:pl-64 min-w-0 pb-20 md:pb-8">
        {/* Top Header */}
        <header className="sticky top-0 z-20 h-16 border-b border-border/80 bg-background/70 backdrop-blur-xl px-4 sm:px-8 flex items-center justify-between gap-4">
          {/* Mobile brand & toggle */}
          <div className="flex items-center gap-3 md:hidden">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="p-2 rounded-lg text-muted hover:text-foreground hover:bg-surface-hover focus:outline-none"
            >
              {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-brand-600" />
              <span className="font-bold text-sm text-foreground">Personal AI</span>
            </div>
          </div>

          {/* Quick AI Search / Command Input */}
          <form
            onSubmit={handleQuickPromptSubmit}
            className="flex-1 max-w-xl hidden sm:flex items-center relative"
          >
            <Sparkles className="w-4 h-4 text-brand-600 absolute left-3.5 pointer-events-none" />
            <input
              type="text"
              value={quickPrompt}
              onChange={(e) => setQuickPrompt(e.target.value)}
              placeholder="Ask AI anything: 'Spent ₹450 on lunch', 'Plan a Goa trip', 'Create task'..."
              className="w-full glass-input bg-surface/60 rounded-xl pl-10 pr-10 py-2 text-xs sm:text-sm text-foreground placeholder:text-muted focus:border-brand-500"
            />
            <button
              type="submit"
              disabled={!quickPrompt.trim()}
              className="absolute right-2 p-1.5 rounded-lg text-muted hover:text-brand-600 hover:bg-surface-hover transition-colors disabled:opacity-30 disabled:pointer-events-none"
            >
              <Send className="w-3.5 h-3.5" />
            </button>
          </form>

          {/* Right Header Icons */}
          <div className="flex items-center gap-3 ml-auto">
            {/* Theme Toggle */}
            <button
              onClick={toggleTheme}
              className="p-2 rounded-xl text-muted hover:text-foreground hover:bg-surface-hover/80 transition-colors border border-border"
              title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
            >
              {theme === 'dark' ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            </button>

            {/* Notification Bell */}
            <button
              onClick={() => setIsNotifOpen(true)}
              className="relative p-2 rounded-xl text-muted hover:text-foreground hover:bg-surface-hover/80 transition-colors border border-border"
            >
              <Bell className="w-4 h-4" />
              {unreadCount > 0 && (
                <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-brand-600 animate-pulse ring-2 ring-slate-950" />
              )}
            </button>

            {/* User profile quick view */}
            <div
              onClick={() => navigate('/settings')}
              className="cursor-pointer flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-xl border border-border hover:border-hover bg-surface/40 transition-colors"
            >
              <div className="w-6 h-6 rounded-lg bg-brand-600/30 text-brand-600 flex items-center justify-center font-bold text-xs">
                {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
              </div>
              <span className="text-xs font-medium text-foreground hidden lg:inline">{user?.name}</span>
            </div>
          </div>
        </header>

        {/* Mobile Dropdown Menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden fixed inset-x-0 top-16 bg-background border-b border-border p-4 z-30 shadow-2xl space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={cn(
                    'flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium',
                    isActive ? 'bg-brand-600/15 text-brand-600' : 'text-muted'
                  )}
                >
                  <Icon className="w-5 h-5" />
                  <span>{item.name}</span>
                </NavLink>
              );
            })}
            <button
              onClick={toggleTheme}
              className="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-foreground w-full text-left"
            >
              {theme === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
              <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>
            </button>
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-rose-400 w-full text-left"
            >
              <LogOut className="w-5 h-5" />
              <span>Sign Out</span>
            </button>
          </div>
        )}

        {/* Page Content */}
        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto animate-fade-in">
          <Outlet />
        </main>
      </div>

      {/* Mobile Bottom Navigation Bar */}
      <div className="md:hidden fixed bottom-0 inset-x-0 bg-background/90 backdrop-blur-xl border-t border-border/80 px-2 py-2 flex items-center justify-around z-30">
        {navItems.slice(0, 5).map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={cn(
                'flex flex-col items-center gap-1 p-2 rounded-xl text-[10px] font-medium transition-colors',
                isActive ? 'text-brand-600 font-semibold' : 'text-muted'
              )}
            >
              <Icon className="w-5 h-5" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </div>

      {/* Notifications Drawer */}
      <NotificationsDrawer
        isOpen={isNotifOpen}
        onClose={() => setIsNotifOpen(false)}
        onNotificationRead={fetchUnreadCount}
      />
    </div>
  );
};
