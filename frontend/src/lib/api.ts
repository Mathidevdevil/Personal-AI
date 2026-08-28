import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import {
  AuthResponse,
  FinanceSummary,
  Expense,
  Income,
  Budget,
  Task,
  TaskSummary,
  Trip,
  TripSummary,
  ItineraryItem,
  Notification,
  Conversation,
  AIChatResponse,
  ApiResponse,
  PagedResponse,
  User,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '';

export const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor for Token Refresh
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url?.includes('/auth/')) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const response = await axios.post<ApiResponse<AuthResponse>>(`${API_BASE_URL}/api/auth/refresh`, {
          refreshToken,
        });

        const newAccessToken = response.data.data.accessToken;
        const newRefreshToken = response.data.data.refreshToken;

        localStorage.setItem('access_token', newAccessToken);
        localStorage.setItem('refresh_token', newRefreshToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }

        processQueue(null, newAccessToken);
        return api(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr as Error, null);
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/login', { email, password });
    return res.data.data;
  },
  register: async (name: string, email: string, password: string, currency?: string): Promise<AuthResponse> => {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/register', { name, email, password, currency });
    return res.data.data;
  },
  me: async (): Promise<User> => {
    const res = await api.get<ApiResponse<User>>('/auth/me');
    return res.data.data;
  },
  logout: async (): Promise<void> => {
    const refreshToken = localStorage.getItem('refresh_token');
    try {
      await api.post('/auth/logout', { refreshToken });
    } finally {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
    }
  },
};

// User API
export const userApi = {
  updateProfile: async (data: { name?: string; timezone?: string }): Promise<User> => {
    const res = await api.put<ApiResponse<User>>('/user/profile', data);
    return res.data.data;
  },
  updateCurrency: async (currency: string): Promise<User> => {
    const res = await api.put<ApiResponse<User>>('/user/currency', { currency });
    return res.data.data;
  },
};

// Finance API
export const financeApi = {
  getSummary: async (month?: number, year?: number): Promise<FinanceSummary> => {
    const params = new URLSearchParams();
    if (month) params.append('month', month.toString());
    if (year) params.append('year', year.toString());
    const res = await api.get<ApiResponse<FinanceSummary>>(`/finance/dashboard?${params.toString()}`);
    return res.data.data;
  },
  getExpenses: async (params?: { category?: string; startDate?: string; endDate?: string; page?: number; size?: number }): Promise<Expense[]> => {
    const res = await api.get<ApiResponse<PagedResponse<Expense> | Expense[]>>('/finance/expenses', { params });
    const d = res.data.data as any;
    if (d && Array.isArray(d.content)) return d.content;
    if (Array.isArray(d)) return d;
    return [];
  },
  createExpense: async (data: { amount: number; category: string; description: string; paymentMethod: string; transactionDate?: string }): Promise<Expense> => {
    const res = await api.post<ApiResponse<Expense>>('/finance/expenses', data);
    return res.data.data;
  },
  updateExpense: async (id: string, data: Partial<Expense>): Promise<Expense> => {
    const res = await api.put<ApiResponse<Expense>>(`/finance/expenses/${id}`, data);
    return res.data.data;
  },
  deleteExpense: async (id: string): Promise<void> => {
    await api.delete(`/finance/expenses/${id}`);
  },
  getIncomes: async (params?: { startDate?: string; endDate?: string }): Promise<Income[]> => {
    const res = await api.get<ApiResponse<PagedResponse<Income> | Income[]>>('/finance/income', { params });
    const d = res.data.data as any;
    if (d && Array.isArray(d.content)) return d.content;
    if (Array.isArray(d)) return d;
    return [];
  },
  createIncome: async (data: { amount: number; source: string; description?: string; incomeDate?: string }): Promise<Income> => {
    const res = await api.post<ApiResponse<Income>>('/finance/income', data);
    return res.data.data;
  },
  deleteIncome: async (id: string): Promise<void> => {
    await api.delete(`/finance/income/${id}`);
  },
  getBudgets: async (month: number, year: number): Promise<Budget[]> => {
    const res = await api.get<ApiResponse<Budget[]>>(`/finance/budgets?month=${month}&year=${year}`);
    return Array.isArray(res.data.data) ? res.data.data : [];
  },
  setBudget: async (data: { category: string; amount: number; month: number; year: number }): Promise<Budget> => {
    const res = await api.post<ApiResponse<Budget>>('/finance/budgets', data);
    return res.data.data;
  },
  deleteBudget: async (id: string): Promise<void> => {
    await api.delete(`/finance/budgets/${id}`);
  },
};

// Task API
export const taskApi = {
  getSummary: async (): Promise<TaskSummary> => {
    const res = await api.get<ApiResponse<TaskSummary>>('/tasks/summary');
    return res.data.data;
  },
  getTasks: async (params?: { status?: string; priority?: string; search?: string; category?: string; page?: number; size?: number }): Promise<Task[]> => {
    const res = await api.get<ApiResponse<PagedResponse<Task> | Task[]>>('/tasks', {
      params: { size: 100, ...params },
    });
    const d = res.data.data as any;
    if (d && Array.isArray(d.content)) return d.content;
    if (Array.isArray(d)) return d;
    return [];
  },
  createTask: async (data: { title: string; description?: string; priority: string; status?: string; dueDate?: string; category?: string }): Promise<Task> => {
    const res = await api.post<ApiResponse<Task>>('/tasks', data);
    return res.data.data;
  },
  updateTask: async (id: string, data: Partial<Task>): Promise<Task> => {
    const res = await api.put<ApiResponse<Task>>(`/tasks/${id}`, data);
    return res.data.data;
  },
  toggleTaskStatus: async (id: string, completed: boolean): Promise<Task> => {
    const res = await api.patch<ApiResponse<Task>>(`/tasks/${id}/complete`);
    return res.data.data;
  },
  deleteTask: async (id: string): Promise<void> => {
    await api.delete(`/tasks/${id}`);
  },
};

// Travel API
export const travelApi = {
  getSummary: async (): Promise<TripSummary> => {
    const res = await api.get<ApiResponse<TripSummary>>('/trips/summary');
    return res.data.data;
  },
  getTrips: async (status?: string): Promise<Trip[]> => {
    const res = await api.get<ApiResponse<PagedResponse<Trip> | Trip[]>>('/trips', {
      params: { size: 50 },
    });
    const d = res.data.data as any;
    if (d && Array.isArray(d.content)) return d.content;
    if (Array.isArray(d)) return d;
    return [];
  },
  getTripById: async (id: string): Promise<Trip> => {
    const res = await api.get<ApiResponse<Trip>>(`/trips/${id}`);
    return res.data.data;
  },
  createTrip: async (data: { name: string; destination: string; startDate: string; endDate: string; budget?: number; currency?: string; description?: string }): Promise<Trip> => {
    const res = await api.post<ApiResponse<Trip>>('/trips', data);
    return res.data.data;
  },
  updateTrip: async (id: string, data: Partial<Trip>): Promise<Trip> => {
    const res = await api.put<ApiResponse<Trip>>(`/trips/${id}`, data);
    return res.data.data;
  },
  deleteTrip: async (id: string): Promise<void> => {
    await api.delete(`/trips/${id}`);
  },
  addItineraryItem: async (tripId: string, data: { dayNumber: number; title: string; description?: string; location?: string; startTime?: string; endTime?: string; estimatedCost?: number; notes?: string }): Promise<ItineraryItem> => {
    const res = await api.post<ApiResponse<ItineraryItem>>(`/trips/${tripId}/itinerary`, data);
    return res.data.data;
  },
  deleteItineraryItem: async (tripId: string, itemId: string): Promise<void> => {
    await api.delete(`/trips/${tripId}/itinerary/${itemId}`);
  },
  generateTripPlan: async (data: { destination: string; days: number; budget?: number; travelers?: number; interests?: string }): Promise<any> => {
    const res = await api.post<ApiResponse<any>>('/trips/generate', data);
    return res.data.data;
  },
};

// AI Assistant API
export const aiApi = {
  chat: async (prompt: string, conversationId?: string): Promise<AIChatResponse> => {
    const res = await api.post<ApiResponse<AIChatResponse>>('/ai/chat', {
      message: prompt,
      conversationId: conversationId || undefined,
    });
    return res.data.data;
  },
  getConversations: async (): Promise<Conversation[]> => {
    const res = await api.get<ApiResponse<Conversation[]>>('/ai/conversations');
    return Array.isArray(res.data.data) ? res.data.data : [];
  },
  getConversation: async (id: string): Promise<Conversation> => {
    const res = await api.get<ApiResponse<Conversation>>(`/ai/conversations/${id}`);
    return res.data.data;
  },
  deleteConversation: async (id: string): Promise<void> => {
    await api.delete(`/ai/conversations/${id}`);
  },
};

// Notification API
export const notificationApi = {
  getNotifications: async (unreadOnly: boolean = false): Promise<Notification[]> => {
    const res = await api.get<ApiResponse<Notification[]>>(`/notifications?unreadOnly=${unreadOnly}`);
    return Array.isArray(res.data.data) ? res.data.data : [];
  },
  markAsRead: async (id: string): Promise<Notification> => {
    const res = await api.put<ApiResponse<Notification>>(`/notifications/${id}/read`);
    return res.data.data;
  },
  markAllAsRead: async (): Promise<void> => {
    await api.put('/notifications/read-all');
  },
};
