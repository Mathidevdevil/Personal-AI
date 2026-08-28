import React, { useState } from 'react';
import {
  Settings,
  User,
  DollarSign,
  Key,
  Shield,
  CheckCircle,
  Save,
  Globe,
  Sparkles,
  Smartphone,
  Database,
} from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';
import { userApi } from '../../lib/api';
import { Button } from '../../components/common/Button';

export const SettingsPage: React.FC = () => {
  const { user, updateUserCurrency, setUser } = useAuthStore();

  const [name, setName] = useState(user?.name || '');
  const [currency, setCurrency] = useState(user?.currency || 'INR');
  const [timezone, setTimezone] = useState(user?.timezone || 'Asia/Kolkata');
  const [customApiKey, setCustomApiKey] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setSavedSuccess(false);

    try {
      if (currency !== user?.currency) {
        await userApi.updateCurrency(currency);
        updateUserCurrency(currency);
      }

      const updatedUser = await userApi.updateProfile({ name, timezone });
      setUser(updatedUser);

      if (customApiKey) {
        localStorage.setItem('custom_ai_api_key', customApiKey);
      }

      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 3000);
    } catch (err) {
      console.error('Failed to update settings:', err);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="max-w-4xl space-y-8 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-foreground tracking-tight">System & Account Settings</h1>
        <p className="text-sm text-muted">Manage your profile, currency formats, and AI provider configurations.</p>
      </div>

      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-sm flex items-center gap-2.5">
          <CheckCircle className="w-5 h-5 shrink-0" />
          <span>Settings saved successfully!</span>
        </div>
      )}

      <form onSubmit={handleSaveProfile} className="space-y-6">
        {/* Profile Card */}
        <div className="glass-card rounded-2xl p-6 border border-border space-y-4">
          <div className="flex items-center gap-3 pb-3 border-b border-border">
            <User className="w-5 h-5 text-brand-600" />
            <h3 className="text-base font-semibold text-foreground">Profile Details</h3>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1.5">
                Email (Account Identifier)
              </label>
              <input
                type="email"
                disabled
                value={user?.email || ''}
                className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-muted bg-surface/50 cursor-not-allowed"
              />
            </div>
          </div>
        </div>

        {/* Currency & Regional Preferences */}
        <div className="glass-card rounded-2xl p-6 border border-border space-y-4">
          <div className="flex items-center gap-3 pb-3 border-b border-border">
            <DollarSign className="w-5 h-5 text-emerald-400" />
            <h3 className="text-base font-semibold text-foreground">Currency & Regional Preferences</h3>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1.5">
                Display Currency
              </label>
              <select
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
                className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground bg-surface"
              >
                <option value="INR">INR (₹) - Indian Rupee</option>
                <option value="USD">USD ($) - US Dollar</option>
                <option value="EUR">EUR (€) - Euro</option>
                <option value="GBP">GBP (£) - British Pound</option>
                <option value="CAD">CAD (CA$) - Canadian Dollar</option>
                <option value="AUD">AUD (A$) - Australian Dollar</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1.5">
                Timezone
              </label>
              <select
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
                className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground bg-surface"
              >
                <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                <option value="UTC">UTC (Coordinated Universal Time)</option>
                <option value="America/New_York">America/New_York (EST)</option>
                <option value="America/Los_Angeles">America/Los_Angeles (PST)</option>
                <option value="Europe/London">Europe/London (GMT)</option>
                <option value="Asia/Tokyo">Asia/Tokyo (JST)</option>
              </select>
            </div>
          </div>
        </div>

        {/* AI Engine Configuration */}
        <div className="glass-card rounded-2xl p-6 border border-border space-y-4">
          <div className="flex items-center gap-3 pb-3 border-b border-border">
            <Sparkles className="w-5 h-5 text-pink-600" />
            <div>
              <h3 className="text-base font-semibold text-foreground">AI Engine Configuration</h3>
              <p className="text-xs text-muted">Customize external LLM integration</p>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-brand-600/10 border border-brand-500/20 text-xs text-indigo-300 space-y-1">
            <p className="font-semibold">Local Deterministic Engine is Active</p>
            <p className="text-muted text-[11px]">
              All core tools (Expense logging, Task creation, Budget queries, Travel planning) work out of the box with zero external API key required.
            </p>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1.5">
              Custom OpenAI / OpenRouter API Key (Optional)
            </label>
            <div className="relative">
              <Key className="w-4 h-4 text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                placeholder="sk-..."
                value={customApiKey}
                onChange={(e) => setCustomApiKey(e.target.value)}
                className="w-full glass-input rounded-xl pl-10 pr-4 py-2.5 text-sm text-foreground placeholder:text-muted"
              />
            </div>
          </div>
        </div>

        {/* Save Button */}
        <div className="flex justify-end">
          <Button
            type="submit"
            variant="primary"
            size="lg"
            isLoading={isSaving}
            leftIcon={<Save className="w-4 h-4" />}
          >
            Save Preferences
          </Button>
        </div>
      </form>

      {/* System Status Info */}
      <div className="glass-card rounded-2xl p-6 border border-border space-y-4">
        <div className="flex items-center gap-3 pb-3 border-b border-border">
          <Shield className="w-5 h-5 text-teal-400" />
          <h3 className="text-base font-semibold text-foreground">System Environment & Diagnostics</h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          <div className="p-3 rounded-xl bg-surface/60 border border-border">
            <div className="flex items-center gap-2 text-muted mb-1">
              <Database className="w-4 h-4 text-brand-600" />
              <span>Database Engine</span>
            </div>
            <p className="font-semibold text-foreground">PostgreSQL / H2 Flyway V1</p>
          </div>

          <div className="p-3 rounded-xl bg-surface/60 border border-border">
            <div className="flex items-center gap-2 text-muted mb-1">
              <Sparkles className="w-4 h-4 text-pink-600" />
              <span>AI Engine</span>
            </div>
            <p className="font-semibold text-foreground">Multi-Tool Rule Engine</p>
          </div>

          <div className="p-3 rounded-xl bg-surface/60 border border-border">
            <div className="flex items-center gap-2 text-muted mb-1">
              <Smartphone className="w-4 h-4 text-teal-400" />
              <span>PWA Support</span>
            </div>
            <p className="font-semibold text-foreground">Service Worker Active</p>
          </div>
        </div>
      </div>
    </div>
  );
};
