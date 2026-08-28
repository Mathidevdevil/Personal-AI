import React, { useState, useEffect } from 'react';
import {
  CheckSquare,
  Plus,
  Search,
  Filter,
  Columns,
  List as ListIcon,
  Clock,
  Calendar,
  AlertCircle,
  CheckCircle2,
  Trash2,
  Edit2,
  MoreVertical,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { taskApi } from '../../lib/api';
import { Task, TaskStatus, TaskPriority, TaskSummary } from '../../types';
import { formatDate, formatRelativeDate, cn } from '../../lib/utils';
import { Badge } from '../../components/common/Badge';
import { Button } from '../../components/common/Button';
import { Modal } from '../../components/common/Modal';
import { EmptyState } from '../../components/common/EmptyState';

const STATUS_COLUMNS: { id: TaskStatus; label: string; color: string }[] = [
  { id: 'TODO', label: 'To Do', color: 'border-hover bg-surface/40' },
  { id: 'IN_PROGRESS', label: 'In Progress', color: 'border-brand-500/30 bg-indigo-950/20' },
  { id: 'COMPLETED', label: 'Completed', color: 'border-emerald-500/30 bg-emerald-950/20' },
];

export const TasksPage: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [summary, setSummary] = useState<TaskSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [viewMode, setViewMode] = useState<'kanban' | 'list'>('kanban');

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');

  // Modal State
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  // Form State
  const [taskForm, setTaskForm] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM' as TaskPriority,
    status: 'TODO' as TaskStatus,
    dueDate: '',
    category: 'General',
  });

  const loadTasks = async () => {
    setIsLoading(true);
    try {
      const [allTasks, sum] = await Promise.all([
        taskApi.getTasks({
          status: statusFilter !== 'ALL' ? statusFilter : undefined,
          priority: priorityFilter !== 'ALL' ? priorityFilter : undefined,
          search: searchQuery.trim() || undefined,
        }),
        taskApi.getSummary(),
      ]);
      setTasks(Array.isArray(allTasks) ? allTasks : []);
      setSummary(sum);
    } catch (err) {
      console.error('Failed to load tasks:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, [statusFilter, priorityFilter, searchQuery]);

  const handleCreateOrUpdateTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!taskForm.title.trim()) return;

    try {
      if (editingTask) {
        await taskApi.updateTask(editingTask.id, {
          ...taskForm,
          dueDate: taskForm.dueDate ? `${taskForm.dueDate}T18:00:00Z` : undefined,
        });
      } else {
        await taskApi.createTask({
          ...taskForm,
          dueDate: taskForm.dueDate ? `${taskForm.dueDate}T18:00:00Z` : undefined,
        });
      }

      setIsCreateModalOpen(false);
      setEditingTask(null);
      setTaskForm({
        title: '',
        description: '',
        priority: 'MEDIUM',
        status: 'TODO',
        dueDate: '',
        category: 'General',
      });
      loadTasks();
    } catch (err) {
      console.error('Failed to save task:', err);
    }
  };

  const handleToggleTask = async (task: Task) => {
    try {
      const nextStatus = task.status !== 'COMPLETED';
      await taskApi.toggleTaskStatus(task.id, nextStatus);

      if (nextStatus) {
        confetti({
          particleCount: 60,
          spread: 70,
          origin: { y: 0.7 },
          colors: ['#6366f1', '#10b981', '#f59e0b', '#ec4899'],
        });
      }
      loadTasks();
    } catch (err) {
      console.error('Failed to toggle task:', err);
    }
  };

  const handleDeleteTask = async (id: string) => {
    try {
      await taskApi.deleteTask(id);
      loadTasks();
    } catch (err) {
      console.error('Failed to delete task:', err);
    }
  };

  const openEditModal = (task: Task) => {
    setEditingTask(task);
    setTaskForm({
      title: task.title,
      description: task.description || '',
      priority: task.priority,
      status: task.status,
      dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
      category: task.category || 'General',
    });
    setIsCreateModalOpen(true);
  };

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-foreground tracking-tight">Tasks & Priorities</h1>
          <p className="text-sm text-muted">Organize workflows, track deadlines, and delegate actions via AI.</p>
        </div>

        <div className="flex items-center gap-3">
          {/* View Mode Toggle */}
          <div className="flex items-center p-1 rounded-xl bg-surface border border-border">
            <button
              onClick={() => setViewMode('kanban')}
              className={cn(
                'p-1.5 rounded-lg text-xs font-medium flex items-center gap-1.5 transition-all',
                viewMode === 'kanban' ? 'bg-brand-600 text-foreground' : 'text-muted hover:text-foreground'
              )}
            >
              <Columns className="w-4 h-4" />
              <span className="hidden sm:inline">Kanban</span>
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={cn(
                'p-1.5 rounded-lg text-xs font-medium flex items-center gap-1.5 transition-all',
                viewMode === 'list' ? 'bg-brand-600 text-foreground' : 'text-muted hover:text-foreground'
              )}
            >
              <ListIcon className="w-4 h-4" />
              <span className="hidden sm:inline">List</span>
            </button>
          </div>

          <Button
            variant="primary"
            size="sm"
            onClick={() => {
              setEditingTask(null);
              setTaskForm({
                title: '',
                description: '',
                priority: 'MEDIUM',
                status: 'TODO',
                dueDate: '',
                category: 'General',
              });
              setIsCreateModalOpen(true);
            }}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Create Task
          </Button>
        </div>
      </div>

      {/* Summary KPI Badges Bar */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="glass-card rounded-xl p-4 border border-border flex items-center justify-between">
          <div>
            <p className="text-xs text-muted">Total Tasks</p>
            <p className="text-xl font-bold text-foreground">{summary?.totalTasks ?? 0}</p>
          </div>
          <CheckSquare className="w-5 h-5 text-brand-600" />
        </div>

        <div className="glass-card rounded-xl p-4 border border-border flex items-center justify-between">
          <div>
            <p className="text-xs text-muted">Due Today</p>
            <p className="text-xl font-bold text-amber-400">{summary?.dueTodayCount ?? 0}</p>
          </div>
          <Clock className="w-5 h-5 text-amber-400" />
        </div>

        <div className="glass-card rounded-xl p-4 border border-border flex items-center justify-between">
          <div>
            <p className="text-xs text-muted">Overdue</p>
            <p className="text-xl font-bold text-rose-400">{summary?.overdueCount ?? 0}</p>
          </div>
          <AlertCircle className="w-5 h-5 text-rose-400" />
        </div>

        <div className="glass-card rounded-xl p-4 border border-border flex items-center justify-between">
          <div>
            <p className="text-xs text-muted">Completed</p>
            <p className="text-xl font-bold text-emerald-400">{summary?.completedCount ?? 0}</p>
          </div>
          <CheckCircle2 className="w-5 h-5 text-emerald-400" />
        </div>
      </div>

      {/* Filters Bar */}
      <div className="glass-card rounded-2xl p-4 border border-border flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search tasks..."
            className="w-full glass-input rounded-xl pl-10 pr-4 py-2 text-xs sm:text-sm text-foreground placeholder:text-muted"
          />
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto">
          <div className="flex items-center gap-2 text-xs">
            <Filter className="w-4 h-4 text-muted" />
            <select
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
              className="glass-input rounded-xl px-3 py-1.5 text-xs text-foreground bg-surface focus:border-brand-500"
            >
              <option value="ALL">All Priorities</option>
              <option value="URGENT">Urgent</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          <div className="flex items-center gap-2 text-xs">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="glass-input rounded-xl px-3 py-1.5 text-xs text-foreground bg-surface focus:border-brand-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="TODO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>
        </div>
      </div>

      {/* Kanban Board View */}
      {viewMode === 'kanban' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {STATUS_COLUMNS.map((col) => {
            const safeTasks = Array.isArray(tasks) ? tasks : [];
            const columnTasks = safeTasks.filter((t) => t.status === col.id);
            return (
              <div
                key={col.id}
                className={cn('rounded-2xl p-4 border flex flex-col min-h-[450px]', col.color)}
              >
                {/* Column Header */}
                <div className="flex items-center justify-between pb-3 border-b border-border/80 mb-3">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-sm text-foreground">{col.label}</span>
                    <span className="text-xs px-2 py-0.5 rounded-full bg-surface-hover text-foreground">
                      {columnTasks.length}
                    </span>
                  </div>
                </div>

                {/* Cards List */}
                <div className="space-y-3 flex-1 overflow-y-auto">
                  {columnTasks.length === 0 ? (
                    <div className="h-32 flex items-center justify-center text-xs text-muted border border-dashed border-border/80 rounded-xl">
                      No tasks in this lane
                    </div>
                  ) : (
                    columnTasks.map((task) => (
                      <div
                        key={task.id}
                        className="glass-card rounded-xl p-4 border border-border/90 glass-card-hover space-y-3 relative group"
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex items-start gap-2.5 min-w-0">
                            <input
                              type="checkbox"
                              checked={task.status === 'COMPLETED'}
                              onChange={() => handleToggleTask(task)}
                              className="mt-1 w-4 h-4 rounded text-indigo-600 bg-surface-hover border-hover focus:ring-indigo-500 cursor-pointer"
                            />
                            <h4
                              className={cn(
                                'text-sm font-semibold text-foreground leading-snug',
                                task.status === 'COMPLETED' && 'line-through text-muted'
                              )}
                            >
                              {task.title}
                            </h4>
                          </div>

                          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0">
                            <button
                              onClick={() => openEditModal(task)}
                              className="p-1 text-muted hover:text-foreground"
                              title="Edit"
                            >
                              <Edit2 className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => handleDeleteTask(task.id)}
                              className="p-1 text-muted hover:text-rose-400"
                              title="Delete"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </div>

                        {task.description && (
                          <p className="text-xs text-muted line-clamp-2 pl-6.5">{task.description}</p>
                        )}

                        <div className="flex items-center justify-between pt-1 border-t border-border/60 text-xs">
                          <Badge priority={task.priority} size="sm" />

                          {task.dueDate && (
                            <div
                              className={cn(
                                'flex items-center gap-1 text-[11px]',
                                task.isOverdue && task.status !== 'COMPLETED'
                                  ? 'text-rose-400 font-semibold'
                                  : 'text-muted'
                              )}
                            >
                              <Calendar className="w-3 h-3" />
                              <span>{formatRelativeDate(task.dueDate)}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* List View */}
      {viewMode === 'list' && (
        <div className="glass-card rounded-2xl p-6 border border-border">
          {(Array.isArray(tasks) ? tasks : []).length === 0 ? (
            <EmptyState
              icon={<CheckSquare className="w-8 h-8" />}
              title="No tasks match your criteria"
              description="Create a task to get started or tell the AI assistant 'Create a task to email the team'."
              actionLabel="Create Task"
              onAction={() => {
                setEditingTask(null);
                setIsCreateModalOpen(true);
              }}
            />
          ) : (
            <div className="divide-y divide-slate-800/80">
              {(Array.isArray(tasks) ? tasks : []).map((task) => (
                <div
                  key={task.id}
                  className="py-4 flex items-center justify-between gap-4 hover:bg-surface-hover/20 px-3 rounded-xl transition-colors"
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
                          'text-sm font-semibold text-foreground truncate',
                          task.status === 'COMPLETED' && 'line-through text-muted'
                        )}
                      >
                        {task.title}
                      </p>
                      {task.description && (
                        <p className="text-xs text-muted truncate">{task.description}</p>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-3 shrink-0">
                    <Badge status={task.status} size="sm" />
                    <Badge priority={task.priority} size="sm" />
                    {task.dueDate && (
                      <span
                        className={cn(
                          'text-xs flex items-center gap-1',
                          task.isOverdue && task.status !== 'COMPLETED'
                            ? 'text-rose-400 font-semibold'
                            : 'text-muted'
                        )}
                      >
                        <Calendar className="w-3.5 h-3.5" />
                        {formatDate(task.dueDate, 'MMM dd')}
                      </span>
                    )}
                    <button
                      onClick={() => openEditModal(task)}
                      className="p-1 text-muted hover:text-foreground"
                      title="Edit"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDeleteTask(task.id)}
                      className="p-1 text-muted hover:text-rose-400"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Create / Edit Task Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title={editingTask ? 'Edit Task' : 'Create New Task'}
        subtitle="Manage task scheduling and priority flags"
      >
        <form onSubmit={handleCreateOrUpdateTask} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Task Title
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Prepare client review presentation"
              value={taskForm.title}
              onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
              Description (Optional)
            </label>
            <textarea
              rows={3}
              placeholder="Add task notes or checklists..."
              value={taskForm.description}
              onChange={(e) => setTaskForm({ ...taskForm, description: e.target.value })}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-sm text-foreground placeholder:text-muted"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Priority
              </label>
              <select
                value={taskForm.priority}
                onChange={(e) => setTaskForm({ ...taskForm, priority: e.target.value as TaskPriority })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Status
              </label>
              <select
                value={taskForm.status}
                onChange={(e) => setTaskForm({ ...taskForm, status: e.target.value as TaskStatus })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Due Date
              </label>
              <input
                type="date"
                value={taskForm.dueDate}
                onChange={(e) => setTaskForm({ ...taskForm, dueDate: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground bg-surface"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-muted uppercase tracking-wider mb-1">
                Category
              </label>
              <input
                type="text"
                placeholder="Work, Personal, etc."
                value={taskForm.category}
                onChange={(e) => setTaskForm({ ...taskForm, category: e.target.value })}
                className="w-full glass-input rounded-xl px-3 py-2.5 text-sm text-foreground placeholder:text-muted"
              />
            </div>
          </div>

          <div className="pt-3 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              {editingTask ? 'Update Task' : 'Create Task'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
