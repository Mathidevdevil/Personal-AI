import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  CheckSquare,
  Plane,
  Sparkles,
  Plus,
  ArrowUpRight,
  Clock,
  AlertCircle,
  Calendar,
  Layers,
  ChevronRight,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import confetti from 'canvas-confetti';
import { useAuthStore } from '../../stores/authStore';
import { financeApi, taskApi, travelApi } from '../../lib/api';
import { FinanceSummary, TaskSummary, TripSummary, Task } from '../../types';
import { formatCurrency, formatDate, cn } from '../../lib/utils';
import { StatCard } from '../../components/common/StatCard';
import { Badge } from '../../components/common/Badge';
import { Button } from '../../components/common/Button';

const COLORS = ['#6366f1', '#ec4899', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#14b8a6'];

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [financeSummary, setFinanceSummary] = useState<FinanceSummary | null>(null);
  const [taskSummary, setTaskSummary] = useState<TaskSummary | null>(null);
  const [tripSummary, setTripSummary] = useState<TripSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [fData, tData, trData] = await Promise.all([
        financeApi.getSummary(),
        taskApi.getSummary(),
        travelApi.getSummary(),
      ]);
      setFinanceSummary(fData);
      setTaskSummary(tData);
      setTripSummary(trData);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleToggleTask = async (task: Task) => {
    try {
      const nextStatus = task.status !== 'COMPLETED';
      await taskApi.toggleTaskStatus(task.id, nextStatus);

      if (nextStatus) {
        confetti({
          particleCount: 50,
          spread: 60,
          origin: { y: 0.8 },
          colors: ['#6366f1', '#10b981', '#f59e0b', '#ec4899'],
        });
      }

      // Refresh task summary
      const updatedSummary = await taskApi.getSummary();
      setTaskSummary(updatedSummary);
    } catch (err) {
      console.error('Failed to toggle task:', err);
    }
  };

  const currency = user?.currency || 'INR';

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-blue-50 via-white to-indigo-50 border border-brand-500/20 p-6 sm:p-8 backdrop-blur-xl">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-brand-600/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-brand-600/20 text-indigo-300 border border-brand-500/30">
                Personal AI Intelligence Suite
              </span>
              <span className="text-xs text-muted">
                {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-foreground tracking-tight">
              Good day, <span className="gradient-text">{user?.name || 'Explorer'}</span>! 👋
            </h1>
            <p className="text-sm text-foreground max-w-xl">
              Your financial balance, tasks, and travel schedules are synchronized with your personal AI.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <Button
              onClick={() => navigate('/assistant')}
              variant="primary"
              size="sm"
              leftIcon={<Sparkles className="w-4 h-4" />}
            >
              Ask AI Assistant
            </Button>
            <Button
              onClick={() => navigate('/finance')}
              variant="secondary"
              size="sm"
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Expense
            </Button>
            <Button
              onClick={() => navigate('/tasks')}
              variant="secondary"
              size="sm"
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Task
            </Button>
          </div>
        </div>
      </div>

      {/* Primary KPI Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard
          title="Current Balance"
          value={formatCurrency(financeSummary?.currentBalance ?? 0, currency)}
          subtitle={`Total Income: ${formatCurrency(financeSummary?.totalIncome ?? 0, currency)}`}
          icon={<Wallet className="w-6 h-6" />}
          glowColor="emerald"
          trend={{
            value: `${financeSummary?.monthlyTrends?.[0]?.netSavings ? (financeSummary.monthlyTrends[0].netSavings >= 0 ? '+' : '') : ''}${formatCurrency(financeSummary?.monthlyTrends?.[0]?.netSavings ?? 0, currency)}`,
            isPositive: (financeSummary?.monthlyTrends?.[0]?.netSavings ?? 0) >= 0,
            label: 'this month savings',
          }}
        />

        <StatCard
          title="Monthly Spending"
          value={formatCurrency(financeSummary?.monthlyExpenses ?? 0, currency)}
          subtitle={`Budget: ${formatCurrency(financeSummary?.totalBudget ?? 0, currency)}`}
          icon={<TrendingDown className="w-6 h-6" />}
          glowColor="indigo"
          trend={{
            value: `${Math.round(financeSummary?.overallBudgetPercentage ?? 0)}%`,
            isPositive: (financeSummary?.overallBudgetPercentage ?? 0) <= 80,
            label: 'budget used',
          }}
        />

        <StatCard
          title="Pending Tasks"
          value={(taskSummary?.todoCount ?? 0) + (taskSummary?.inProgressCount ?? 0)}
          subtitle={`${taskSummary?.completedCount ?? 0} tasks completed`}
          icon={<CheckSquare className="w-6 h-6" />}
          glowColor="amber"
          trend={{
            value: `${taskSummary?.overdueCount ?? 0}`,
            isPositive: (taskSummary?.overdueCount ?? 0) === 0,
            label: 'overdue tasks',
          }}
        />

        <StatCard
          title="Travel & Trips"
          value={tripSummary?.plannedTrips ?? 0}
          subtitle={tripSummary?.nextUpcomingTrip ? `Next: ${tripSummary.nextUpcomingTrip.destination}` : 'No upcoming trip'}
          icon={<Plane className="w-6 h-6" />}
          glowColor="cyan"
          trend={{
            value: `${tripSummary?.totalTrips ?? 0}`,
            isPositive: true,
            label: 'total logged',
          }}
        />
      </div>

      {/* Main Charts & Analytics Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Monthly Trend Area Chart */}
        <div className="lg:col-span-2 glass-card rounded-2xl p-6 border border-border min-w-0 overflow-hidden">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-base font-semibold text-foreground">Income vs Expense Trends</h3>
              <p className="text-xs text-muted">Monthly financial flow</p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/finance')}
              rightIcon={<ArrowUpRight className="w-3.5 h-3.5" />}
            >
              Details
            </Button>
          </div>

          <div className="h-64 sm:h-72 w-full">
            {financeSummary?.monthlyTrends && financeSummary.monthlyTrends.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart
                  data={financeSummary.monthlyTrends}
                  margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
                >
                  <defs>
                    <linearGradient id="incomeGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                    </linearGradient>
                    <linearGradient id="expenseGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#6366f1" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.3} />
                  <XAxis dataKey="monthName" stroke="#64748b" fontSize={11} tickLine={false} />
                  <YAxis stroke="#64748b" fontSize={11} tickLine={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#ffffff',
                      borderColor: '#e2e8f0',
                      borderRadius: '0.75rem',
                      fontSize: '12px',
                      color: '#0f172a'
                    }}
                    formatter={(value: any) => [formatCurrency(Number(value), currency), '']}
                  />
                  <Area
                    type="monotone"
                    dataKey="income"
                    name="Income"
                    stroke="#10b981"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#incomeGrad)"
                  />
                  <Area
                    type="monotone"
                    dataKey="expense"
                    name="Expense"
                    stroke="#6366f1"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#expenseGrad)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-muted text-xs">
                No monthly trend data recorded yet.
              </div>
            )}
          </div>
        </div>

        {/* Category Breakdown Pie Chart */}
        <div className="glass-card rounded-2xl p-6 border border-border flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-base font-semibold text-foreground">Expense Distribution</h3>
                <p className="text-xs text-muted">By category this month</p>
              </div>
            </div>

            <div className="h-44 w-full flex items-center justify-center">
              {financeSummary?.categoryBreakdown && financeSummary.categoryBreakdown.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={financeSummary.categoryBreakdown}
                      cx="50%"
                      cy="50%"
                      innerRadius={45}
                      outerRadius={70}
                      paddingAngle={4}
                      dataKey="spentAmount"
                      nameKey="category"
                    >
                      {financeSummary.categoryBreakdown.map((_, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#ffffff',
                        borderColor: '#e2e8f0',
                        borderRadius: '0.75rem',
                        fontSize: '12px',
                        color: '#0f172a'
                      }}
                      formatter={(val: any) => formatCurrency(Number(val), currency)}
                    />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="text-muted text-xs">No categorical expenses this month</div>
              )}
            </div>
          </div>

          {/* Category Mini Legend */}
          <div className="space-y-2 mt-2 pt-4 border-t border-border/80">
            {financeSummary?.categoryBreakdown?.slice(0, 3).map((cat, idx) => (
              <div key={cat.category} className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <div
                    className="w-2.5 h-2.5 rounded-full"
                    style={{ backgroundColor: COLORS[idx % COLORS.length] }}
                  />
                  <span className="text-foreground font-medium">{cat.category}</span>
                </div>
                <span className="text-foreground font-semibold">{formatCurrency(cat.spentAmount, currency)}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Two Column Grid: Today's Tasks & Travel/AI suggestions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Today's & Overdue Tasks */}
        <div className="glass-card rounded-2xl p-6 border border-border">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <CheckSquare className="w-5 h-5 text-brand-600" />
              <h3 className="text-base font-semibold text-foreground">Focus & Urgent Tasks</h3>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/tasks')}
              rightIcon={<ChevronRight className="w-3.5 h-3.5" />}
            >
              View All
            </Button>
          </div>

          <div className="space-y-2.5">
            {taskSummary?.overdueTasks && taskSummary.overdueTasks.length > 0 && (
              <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 mb-3">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-rose-400 mb-2">
                  <AlertCircle className="w-4 h-4" /> Overdue Tasks ({taskSummary.overdueTasks.length})
                </div>
                <div className="space-y-1.5">
                  {taskSummary.overdueTasks.slice(0, 2).map((t) => (
                    <div key={t.id} className="flex items-center justify-between text-xs">
                      <div className="flex items-center gap-2 truncate">
                        <input
                          type="checkbox"
                          checked={t.status === 'COMPLETED'}
                          onChange={() => handleToggleTask(t)}
                          className="rounded text-indigo-600 bg-surface border-hover"
                        />
                        <span className="text-foreground truncate">{t.title}</span>
                      </div>
                      <Badge priority={t.priority} size="sm" />
                    </div>
                  ))}
                </div>
              </div>
            )}

            {taskSummary?.todayTasks && taskSummary.todayTasks.length > 0 ? (
              taskSummary.todayTasks.slice(0, 4).map((task) => (
                <div
                  key={task.id}
                  className="flex items-center justify-between p-3 rounded-xl bg-surface/50 border border-border/80 hover:border-hover transition-colors"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <input
                      type="checkbox"
                      checked={task.status === 'COMPLETED'}
                      onChange={() => handleToggleTask(task)}
                      className="w-4 h-4 rounded text-indigo-600 bg-surface-hover border-hover focus:ring-indigo-500 cursor-pointer"
                    />
                    <div className="min-w-0">
                      <p
                        className={cn(
                          'text-sm font-medium truncate',
                          task.status === 'COMPLETED' ? 'line-through text-muted' : 'text-foreground'
                        )}
                      >
                        {task.title}
                      </p>
                      <p className="text-[11px] text-muted">{task.category || 'General'}</p>
                    </div>
                  </div>
                  <Badge priority={task.priority} size="sm" />
                </div>
              ))
            ) : (
              <div className="p-6 text-center text-xs text-muted border border-dashed border-border rounded-xl">
                No tasks scheduled for today. You are all caught up!
              </div>
            )}
          </div>
        </div>

        {/* Travel Card & Suggested AI Commands */}
        <div className="space-y-6">
          {/* Next Trip Card */}
          {tripSummary?.nextUpcomingTrip ? (
            <div className="glass-card rounded-2xl p-6 border border-border relative overflow-hidden">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <Plane className="w-5 h-5 text-teal-400" />
                  <h3 className="text-base font-semibold text-foreground">Next Adventure</h3>
                </div>
                <Badge status={tripSummary.nextUpcomingTrip.status} size="sm" />
              </div>

              <div className="space-y-2">
                <h4 className="text-lg font-bold text-foreground tracking-tight">
                  {tripSummary.nextUpcomingTrip.name}
                </h4>
                <div className="flex items-center gap-4 text-xs text-foreground">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-teal-400" />
                    {formatDate(tripSummary.nextUpcomingTrip.startDate, 'MMM dd')} - {formatDate(tripSummary.nextUpcomingTrip.endDate, 'MMM dd, yyyy')}
                  </span>
                  <span className="font-semibold text-teal-400">
                    {tripSummary.nextUpcomingTrip.totalDays} Days
                  </span>
                </div>
                <div className="pt-2 flex items-center justify-between text-xs text-muted border-t border-border mt-3">
                  <span>Budget: {formatCurrency(tripSummary.nextUpcomingTrip.budget, tripSummary.nextUpcomingTrip.currency)}</span>
                  <button
                    onClick={() => navigate('/travel')}
                    className="text-brand-600 hover:text-indigo-300 font-medium flex items-center gap-1"
                  >
                    View Itinerary →
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="glass-card rounded-2xl p-6 border border-border text-center">
              <Plane className="w-8 h-8 text-teal-400/60 mx-auto mb-2" />
              <h4 className="text-sm font-semibold text-foreground">No Upcoming Trips</h4>
              <p className="text-xs text-muted mt-1 mb-4">Let AI create a custom vacation itinerary for you in seconds.</p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => navigate('/travel')}
                leftIcon={<Sparkles className="w-3.5 h-3.5 text-brand-600" />}
              >
                Plan a Trip with AI
              </Button>
            </div>
          )}

          {/* AI Quick Prompts Card */}
          <div className="glass-card rounded-2xl p-5 border border-brand-500/20 bg-gradient-to-br from-white via-slate-50 to-indigo-50/50">
            <div className="flex items-center gap-2 mb-3">
              <Sparkles className="w-4 h-4 text-pink-600" />
              <h4 className="text-xs font-semibold text-foreground uppercase tracking-wider">Try Asking Personal AI</h4>
            </div>
            <div className="flex flex-wrap gap-2">
              {[
                'Add ₹650 for dinner via UPI',
                'How much did I spend this month?',
                'Create task: Prepare client proposal by Friday',
                'Plan 3-day trip to Goa under ₹20,000',
              ].map((promptText) => (
                <button
                  key={promptText}
                  onClick={() => navigate('/assistant', { state: { initialPrompt: promptText } })}
                  className="text-[11px] px-3 py-1.5 rounded-lg bg-surface-hover/80 hover:bg-brand-600/20 border border-hover hover:border-brand-500/40 text-foreground hover:text-foreground transition-all text-left"
                >
                  💬 {promptText}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
