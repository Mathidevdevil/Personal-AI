import React, { useState, useEffect } from 'react';
import {
  Wallet,
  Plus,
  ArrowDownLeft,
  ArrowUpRight,
  Filter,
  Trash2,
  PieChart as PieIcon,
  CreditCard,
  Calendar,
  AlertTriangle,
  DollarSign,
} from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';
import { financeApi } from '../../lib/api';
import {
  Expense,
  Income,
  Budget,
  FinanceSummary,
  ExpenseCategory,
  PaymentMethod,
} from '../../types';
import { formatCurrency, formatDate, cn } from '../../lib/utils';
import { StatCard } from '../../components/common/StatCard';
import { Badge } from '../../components/common/Badge';
import { Button } from '../../components/common/Button';
import { Modal } from '../../components/common/Modal';
import { EmptyState } from '../../components/common/EmptyState';

const CATEGORIES: ExpenseCategory[] = [
  'FOOD',
  'TRANSPORT',
  'SHOPPING',
  'BILLS',
  'ENTERTAINMENT',
  'EDUCATION',
  'HEALTH',
  'TRAVEL',
  'OTHER',
];

const PAYMENT_METHODS: PaymentMethod[] = ['UPI', 'CARD', 'CASH', 'BANK_TRANSFER', 'OTHER'];

export const FinancePage: React.FC = () => {
  const { user } = useAuthStore();
  const currency = user?.currency || 'INR';

  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const [activeTab, setActiveTab] = useState<'expenses' | 'income' | 'budgets'>('expenses');

  const [summary, setSummary] = useState<FinanceSummary | null>(null);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [incomes, setIncomes] = useState<Income[]>([]);
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modals
  const [isAddExpenseOpen, setIsAddExpenseOpen] = useState(false);
  const [isAddIncomeOpen, setIsAddIncomeOpen] = useState(false);
  const [isSetBudgetOpen, setIsSetBudgetOpen] = useState(false);

  // Form States
  const [expenseForm, setExpenseForm] = useState({
    amount: '',
    category: 'FOOD' as ExpenseCategory,
    description: '',
    paymentMethod: 'UPI' as PaymentMethod,
    transactionDate: new Date().toISOString().split('T')[0],
  });

  const [incomeForm, setIncomeForm] = useState({
    amount: '',
    source: '',
    description: '',
    incomeDate: new Date().toISOString().split('T')[0],
  });

  const [budgetForm, setBudgetForm] = useState({
    category: 'FOOD' as ExpenseCategory,
    amount: '',
  });

  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('ALL');

  const loadFinanceData = async () => {
    setIsLoading(true);
    try {
      const [sum, expList, incList, bgtList] = await Promise.all([
        financeApi.getSummary(currentMonth, currentYear),
        financeApi.getExpenses(selectedCategoryFilter !== 'ALL' ? { category: selectedCategoryFilter } : undefined),
        financeApi.getIncomes(),
        financeApi.getBudgets(currentMonth, currentYear),
      ]);
      setSummary(sum);
      setExpenses(Array.isArray(expList) ? expList : []);
      setIncomes(Array.isArray(incList) ? incList : []);
      setBudgets(Array.isArray(bgtList) ? bgtList : []);
    } catch (err) {
      console.error('Failed to fetch finance data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadFinanceData();
  }, [currentMonth, currentYear, selectedCategoryFilter]);

  const handleCreateExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!expenseForm.amount || !expenseForm.description) return;

    try {
      await financeApi.createExpense({
        amount: parseFloat(expenseForm.amount),
        category: expenseForm.category,
        description: expenseForm.description,
        paymentMethod: expenseForm.paymentMethod,
        transactionDate: `${expenseForm.transactionDate}T12:00:00Z`,
      });
      setIsAddExpenseOpen(false);
      setExpenseForm({
        amount: '',
        category: 'FOOD',
        description: '',
        paymentMethod: 'UPI',
        transactionDate: new Date().toISOString().split('T')[0],
      });
      loadFinanceData();
    } catch (err) {
      console.error('Failed to create expense:', err);
    }
  };

  const handleCreateIncome = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!incomeForm.amount || !incomeForm.source) return;

    try {
      await financeApi.createIncome({
        amount: parseFloat(incomeForm.amount),
        source: incomeForm.source,
        description: incomeForm.description,
        incomeDate: `${incomeForm.incomeDate}T12:00:00Z`,
      });
      setIsAddIncomeOpen(false);
      setIncomeForm({
        amount: '',
        source: '',
        description: '',
        incomeDate: new Date().toISOString().split('T')[0],
      });
      loadFinanceData();
    } catch (err) {
      console.error('Failed to create income:', err);
    }
  };

  const handleSetBudget = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!budgetForm.amount) return;

    try {
      await financeApi.setBudget({
        category: budgetForm.category,
        amount: parseFloat(budgetForm.amount),
        month: currentMonth,
        year: currentYear,
      });
      setIsSetBudgetOpen(false);
      setBudgetForm({ category: 'FOOD', amount: '' });
      loadFinanceData();
    } catch (err) {
      console.error('Failed to set budget:', err);
    }
  };

  const handleDeleteExpense = async (id: string) => {
    try {
      await financeApi.deleteExpense(id);
      loadFinanceData();
    } catch (err) {
      console.error('Failed to delete expense:', err);
    }
  };

  const handleDeleteIncome = async (id: string) => {
    try {
      await financeApi.deleteIncome(id);
      loadFinanceData();
    } catch (err) {
      console.error('Failed to delete income:', err);
    }
  };

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-foreground tracking-tight">Finance Management</h1>
          <p className="text-sm text-muted">Track multi-category expenses, income streams, and monthly budgets.</p>
        </div>

        <div className="flex items-center gap-2.5 flex-wrap">
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsAddExpenseOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Add Expense
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setIsAddIncomeOpen(true)}
            leftIcon={<ArrowDownLeft className="w-4 h-4 text-emerald-400" />}
          >
            Add Income
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsSetBudgetOpen(true)}
            leftIcon={<PieIcon className="w-4 h-4 text-brand-600" />}
          >
            Set Budget
          </Button>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard
          title="Total Balance"
          value={formatCurrency(summary?.currentBalance ?? 0, currency)}
          subtitle="Net savings to date"
          icon={<Wallet className="w-6 h-6" />}
          glowColor="emerald"
        />
        <StatCard
          title="Monthly Income"
          value={formatCurrency(summary?.monthlyIncome ?? 0, currency)}
          subtitle={`Total: ${formatCurrency(summary?.totalIncome ?? 0, currency)}`}
          icon={<ArrowDownLeft className="w-6 h-6" />}
          glowColor="cyan"
        />
        <StatCard
          title="Monthly Expenses"
          value={formatCurrency(summary?.monthlyExpenses ?? 0, currency)}
          subtitle={`Total: ${formatCurrency(summary?.totalExpenses ?? 0, currency)}`}
          icon={<ArrowUpRight className="w-6 h-6" />}
          glowColor="rose"
        />
        <StatCard
          title="Budget Allocation"
          value={formatCurrency(summary?.totalBudget ?? 0, currency)}
          subtitle={`${Math.round(summary?.overallBudgetPercentage ?? 0)}% spent this month`}
          icon={<PieIcon className="w-6 h-6" />}
          glowColor="indigo"
        />
      </div>

      {/* Budget Gauges Section */}
      {summary?.categoryBreakdown && summary.categoryBreakdown.length > 0 && (
        <div className="glass-card rounded-2xl p-6 border border-border space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-semibold text-foreground">Monthly Category Budgets</h3>
              <p className="text-xs text-muted">Spending progress against your set monthly limits</p>
            </div>
            <div className="text-xs text-muted">
              Month: <span className="font-semibold text-foreground">{currentMonth}/{currentYear}</span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 pt-2">
            {summary.categoryBreakdown.map((cat) => {
              const percentage = Math.min(100, Math.round(cat.budgetUsagePercentage || 0));
              const isOver = (cat.budgetUsagePercentage || 0) > 100;
              return (
                <div
                  key={cat.category}
                  className="p-4 rounded-xl bg-surface/50 border border-border space-y-2.5"
                >
                  <div className="flex items-center justify-between">
                    <Badge category={cat.category} size="sm" />
                    <span className={cn('text-xs font-bold', isOver ? 'text-rose-400' : 'text-foreground')}>
                      {percentage}%
                    </span>
                  </div>

                  {/* Progress bar */}
                  <div className="w-full bg-surface-hover h-2 rounded-full overflow-hidden">
                    <div
                      className={cn(
                        'h-full rounded-full transition-all duration-500',
                        isOver
                          ? 'bg-rose-500'
                          : percentage > 80
                          ? 'bg-amber-500'
                          : 'bg-brand-600'
                      )}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-xs text-muted pt-1">
                    <span>Spent: <strong className="text-foreground">{formatCurrency(cat.spentAmount, currency)}</strong></span>
                    <span>Budget: <strong className="text-foreground">{formatCurrency(cat.budgetAmount, currency)}</strong></span>
                  </div>

                  {isOver && (
                    <div className="flex items-center gap-1 text-[11px] text-rose-400 font-medium">
                      <AlertTriangle className="w-3 h-3" /> Exceeded by {formatCurrency(Math.abs(cat.remainingAmount), currency)}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Tabs & Ledger Section */}
      <div className="glass-card rounded-2xl p-6 border border-border space-y-6">
        {/* Navigation Tabs & Filter */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border pb-4">
          <div className="flex gap-2">
            <button
              onClick={() => setActiveTab('expenses')}
              className={cn(
                'px-4 py-2 rounded-xl text-sm font-medium transition-all',
                activeTab === 'expenses'
                  ? 'bg-brand-600 text-foreground shadow-lg shadow-indigo-600/25'
                  : 'text-muted hover:text-foreground hover:bg-surface-hover/60'
              )}
            >
              Expenses Ledger ({expenses.length})
            </button>
            <button
              onClick={() => setActiveTab('income')}
              className={cn(
                'px-4 py-2 rounded-xl text-sm font-medium transition-all',
                activeTab === 'income'
                  ? 'bg-brand-600 text-foreground shadow-lg shadow-indigo-600/25'
                  : 'text-muted hover:text-foreground hover:bg-surface-hover/60'
              )}
            >
              Income Ledger ({incomes.length})
            </button>
            <button
              onClick={() => setActiveTab('budgets')}
              className={cn(
                'px-4 py-2 rounded-xl text-sm font-medium transition-all',
                activeTab === 'budgets'
                  ? 'bg-brand-600 text-foreground shadow-lg shadow-indigo-600/25'
                  : 'text-muted hover:text-foreground hover:bg-surface-hover/60'
              )}
            >
              Budgets ({budgets.length})
            </button>
          </div>

          {activeTab === 'expenses' && (
            <div className="flex items-center gap-2">
              <Filter className="w-4 h-4 text-muted" />
              <select
                value={selectedCategoryFilter}
                onChange={(e) => setSelectedCategoryFilter(e.target.value)}
                className="glass-input rounded-xl px-3 py-1.5 text-xs text-foreground bg-surface focus:border-brand-500"
              >
                <option value="ALL">All Categories</option>
                {CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>

        {/* Tab 1: Expenses Ledger Table */}
        {activeTab === 'expenses' && (
          <div>
            {expenses.length === 0 ? (
              <EmptyState
                icon={<Wallet className="w-8 h-8" />}
                title="No expenses logged yet"
                description="Add your daily expenses or tell the AI assistant 'Add ₹500 for groceries'."
                actionLabel="Add Expense"
                onAction={() => setIsAddExpenseOpen(true)}
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs sm:text-sm">
                  <thead className="text-muted uppercase text-[11px] font-semibold border-b border-border">
                    <tr>
                      <th className="pb-3 px-3">Description</th>
                      <th className="pb-3 px-3">Category</th>
                      <th className="pb-3 px-3">Payment</th>
                      <th className="pb-3 px-3">Date</th>
                      <th className="pb-3 px-3 text-right">Amount</th>
                      <th className="pb-3 px-3 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {expenses.map((item) => (
                      <tr key={item.id} className="hover:bg-surface-hover/30 transition-colors">
                        <td className="py-3.5 px-3 font-medium text-foreground">{item.description}</td>
                        <td className="py-3.5 px-3">
                          <Badge category={item.category} size="sm" />
                        </td>
                        <td className="py-3.5 px-3 text-foreground">
                          <span className="px-2 py-0.5 rounded bg-surface-hover text-[11px] border border-hover">
                            {item.paymentMethod}
                          </span>
                        </td>
                        <td className="py-3.5 px-3 text-muted">{formatDate(item.transactionDate)}</td>
                        <td className="py-3.5 px-3 text-right font-bold text-rose-400">
                          -{formatCurrency(item.amount, currency)}
                        </td>
                        <td className="py-3.5 px-3 text-right">
                          <button
                            onClick={() => handleDeleteExpense(item.id)}
                            className="p-1 text-muted hover:text-rose-400 transition-colors"
                            title="Delete"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Income Ledger Table */}
        {activeTab === 'income' && (
          <div>
            {incomes.length === 0 ? (
              <EmptyState
                icon={<ArrowDownLeft className="w-8 h-8 text-emerald-400" />}
                title="No income records"
                description="Log your salary, freelance earnings, or dividend payouts."
                actionLabel="Add Income"
                onAction={() => setIsAddIncomeOpen(true)}
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs sm:text-sm">
                  <thead className="text-muted uppercase text-[11px] font-semibold border-b border-border">
                    <tr>
                      <th className="pb-3 px-3">Source</th>
                      <th className="pb-3 px-3">Description</th>
                      <th className="pb-3 px-3">Date</th>
                      <th className="pb-3 px-3 text-right">Amount</th>
                      <th className="pb-3 px-3 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {incomes.map((item) => (
                      <tr key={item.id} className="hover:bg-surface-hover/30 transition-colors">
                        <td className="py-3.5 px-3 font-semibold text-foreground">{item.source}</td>
                        <td className="py-3.5 px-3 text-foreground">{item.description || '-'}</td>
                        <td className="py-3.5 px-3 text-muted">{formatDate(item.incomeDate)}</td>
                        <td className="py-3.5 px-3 text-right font-bold text-emerald-400">
                          +{formatCurrency(item.amount, currency)}
                        </td>
                        <td className="py-3.5 px-3 text-right">
                          <button
                            onClick={() => handleDeleteIncome(item.id)}
                            className="p-1 text-muted hover:text-rose-400 transition-colors"
                            title="Delete"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 3: Budgets Management */}
        {activeTab === 'budgets' && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <p className="text-xs text-muted">Budgets defined for {currentMonth}/{currentYear}</p>
              <Button size="sm" onClick={() => setIsSetBudgetOpen(true)} leftIcon={<Plus className="w-3.5 h-3.5" />}>
                Add Category Limit
              </Button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {budgets.map((b) => (
                <div key={b.id} className="p-4 rounded-xl glass-card border border-border flex justify-between items-center">
                  <div>
                    <Badge category={b.category} size="sm" />
                    <p className="text-lg font-bold text-foreground mt-2">{formatCurrency(b.amount, currency)}</p>
                    <p className="text-xs text-muted mt-0.5">Month: {b.month}/{b.year}</p>
                  </div>
                  <button
                    onClick={async () => {
                      await financeApi.deleteBudget(b.id);
                      loadFinanceData();
                    }}
                    className="p-2 text-muted hover:text-rose-400 rounded-lg hover:bg-rose-500/10 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Add Expense Modal */}
      <Modal
        isOpen={isAddExpenseOpen}
        onClose={() => setIsAddExpenseOpen(false)}
        title="Record New Expense"
        subtitle="Log an expense transaction to update budgets and analytics"
      >
        <form onSubmit={handleCreateExpense} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Amount ({currency})
            </label>
            <input
              type="number"
              step="0.01"
              required
              placeholder="e.g. 450"
              value={expenseForm.amount}
              onChange={(e) => setExpenseForm({ ...expenseForm, amount: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Category
            </label>
            <select
              value={expenseForm.category}
              onChange={(e) => setExpenseForm({ ...expenseForm, category: e.target.value as ExpenseCategory })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground bg-surface focus:border-brand-500"
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Description
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Dinner with team at Bistro"
              value={expenseForm.description}
              onChange={(e) => setExpenseForm({ ...expenseForm, description: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Payment Method
              </label>
              <select
                value={expenseForm.paymentMethod}
                onChange={(e) => setExpenseForm({ ...expenseForm, paymentMethod: e.target.value as PaymentMethod })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              >
                {PAYMENT_METHODS.map((pm) => (
                  <option key={pm} value={pm}>{pm}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Transaction Date
              </label>
              <input
                type="date"
                required
                value={expenseForm.transactionDate}
                onChange={(e) => setExpenseForm({ ...expenseForm, transactionDate: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              >
              </input>
            </div>
          </div>

          <div className="pt-2 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsAddExpenseOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              Save Expense
            </Button>
          </div>
        </form>
      </Modal>

      {/* Add Income Modal */}
      <Modal
        isOpen={isAddIncomeOpen}
        onClose={() => setIsAddIncomeOpen(false)}
        title="Record Income"
        subtitle="Log incoming revenue, salary, or investments"
      >
        <form onSubmit={handleCreateIncome} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Amount ({currency})
            </label>
            <input
              type="number"
              step="0.01"
              required
              placeholder="e.g. 50000"
              value={incomeForm.amount}
              onChange={(e) => setIncomeForm({ ...incomeForm, amount: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Income Source
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Monthly Salary, Freelance Client"
              value={incomeForm.source}
              onChange={(e) => setIncomeForm({ ...incomeForm, source: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Description (Optional)
            </label>
            <input
              type="text"
              placeholder="e.g. Direct bank transfer"
              value={incomeForm.description}
              onChange={(e) => setIncomeForm({ ...incomeForm, description: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Date
            </label>
            <input
              type="date"
              required
              value={incomeForm.incomeDate}
              onChange={(e) => setIncomeForm({ ...incomeForm, incomeDate: e.target.value })}
              className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
            />
          </div>

          <div className="pt-2 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsAddIncomeOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              Record Income
            </Button>
          </div>
        </form>
      </Modal>

      {/* Set Budget Modal */}
      <Modal
        isOpen={isSetBudgetOpen}
        onClose={() => setIsSetBudgetOpen(false)}
        title="Set Category Budget"
        subtitle={`Set monthly spending target for ${currentMonth}/${currentYear}`}
      >
        <form onSubmit={handleSetBudget} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Category
            </label>
            <select
              value={budgetForm.category}
              onChange={(e) => setBudgetForm({ ...budgetForm, category: e.target.value as ExpenseCategory })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground bg-surface"
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Target Monthly Limit ({currency})
            </label>
            <input
              type="number"
              step="0.01"
              required
              placeholder="e.g. 10000"
              value={budgetForm.amount}
              onChange={(e) => setBudgetForm({ ...budgetForm, amount: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="pt-2 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsSetBudgetOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              Save Budget
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
