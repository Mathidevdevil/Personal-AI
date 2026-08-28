export interface User {
  id: string;
  name: string;
  email: string;
  currency: string;
  timezone: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export type ExpenseCategory =
  | 'FOOD'
  | 'TRANSPORT'
  | 'SHOPPING'
  | 'BILLS'
  | 'ENTERTAINMENT'
  | 'EDUCATION'
  | 'HEALTH'
  | 'TRAVEL'
  | 'OTHER';

export type PaymentMethod =
  | 'CASH'
  | 'UPI'
  | 'CARD'
  | 'BANK_TRANSFER'
  | 'OTHER';

export interface Expense {
  id: string;
  amount: number;
  category: ExpenseCategory;
  description: string;
  paymentMethod: PaymentMethod;
  transactionDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface Income {
  id: string;
  amount: number;
  source: string;
  description?: string;
  incomeDate: string;
  createdAt: string;
}

export interface Budget {
  id: string;
  category: ExpenseCategory;
  amount: number;
  spentAmount: number;
  remainingAmount: number;
  percentageUsed: number;
  month: number;
  year: number;
  createdAt: string;
  updatedAt: string;
}

export type BudgetResponse = Budget;

export interface CategorySpending {
  category: ExpenseCategory;
  spentAmount: number;
  budgetAmount: number;
  remainingAmount: number;
  percentageOfTotal: number;
  budgetUsagePercentage: number;
}

export interface MonthlyTrend {
  monthName: string;
  month: number;
  year: number;
  expense: number;
  income: number;
  netSavings: number;
}

export interface FinanceSummary {
  totalIncome: number;
  totalExpenses: number;
  currentBalance: number;
  monthlyIncome: number;
  monthlyExpenses: number;
  totalBudget: number;
  totalBudgetSpent: number;
  remainingBudget: number;
  overallBudgetPercentage: number;
  categoryBreakdown: CategorySpending[];
  monthlyTrends: MonthlyTrend[];
  recentTransactions: Expense[];
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
  category?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
  isOverdue: boolean;
}

export interface TaskSummary {
  totalTasks: number;
  todoCount: number;
  inProgressCount: number;
  completedCount: number;
  overdueCount: number;
  dueTodayCount: number;
  todayTasks: Task[];
  overdueTasks: Task[];
  highPriorityTasks: Task[];
  upcomingTasks: Task[];
}

export type TripStatus = 'PLANNED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface ItineraryItem {
  id: string;
  dayNumber: number;
  title: string;
  description?: string;
  location?: string;
  startTime?: string;
  endTime?: string;
  estimatedCost: number;
  notes?: string;
  createdAt: string;
}

export interface Trip {
  id: string;
  name: string;
  destination: string;
  startDate: string;
  endDate: string;
  budget: number;
  currency: string;
  description?: string;
  status: TripStatus;
  totalEstimatedCost: number;
  remainingBudget: number;
  totalDays: number;
  itineraryItems: ItineraryItem[];
  createdAt: string;
  updatedAt: string;
}

export interface TripSummary {
  totalTrips: number;
  plannedTrips: number;
  activeTrips: number;
  completedTrips: number;
  nextUpcomingTrip?: Trip;
  recentTrips: Trip[];
}

export type NotificationType =
  | 'TASK_DUE'
  | 'TASK_OVERDUE'
  | 'BUDGET_WARNING'
  | 'TRIP_REMINDER'
  | 'SYSTEM';

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
}

export type MessageRole = 'USER' | 'ASSISTANT' | 'TOOL' | 'SYSTEM';

export interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  toolCallJson?: string;
  createdAt: string;
}

export interface ToolResult {
  toolName: string;
  success: boolean;
  message: string;
  data?: any;
  module: string;
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: ChatMessage[];
}

export interface AIChatResponse {
  conversationId: string;
  conversationTitle: string;
  message: ChatMessage;
  toolResults: ToolResult[];
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
