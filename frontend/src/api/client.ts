import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      const userId = localStorage.getItem('userId');
      
      if (refreshToken && userId) {
        try {
          const response = await axios.post('/api/auth/refresh', {
            refreshToken,
            userId: parseInt(userId),
          });
          
          const { token, refreshToken: newRefreshToken } = response.data.data;
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', newRefreshToken);
          
          error.config.headers.Authorization = `Bearer ${token}`;
          return api(error.config);
        } catch {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('userId');
          window.location.href = '/login';
        }
      } else {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userId');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  type: string;
  userId: number;
  username: string;
  email: string;
}

export interface Transaction {
  id: number;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  transactionDate: string;
  description: string;
  note: string;
  isRecurring: boolean;
  recurringFrequency: string;
  createdAt: string;
  updatedAt: string;
  accountId: number;
  accountName: string;
  categoryId: number;
  categoryName: string;
}

export interface TransactionRequest {
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  transactionDate: string;
  description: string;
  note?: string;
  isRecurring?: boolean;
  recurringFrequency?: string;
  accountId: number;
  categoryId: number;
}

export interface TransactionSummary {
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
}

export interface Account {
  id: number;
  name: string;
  type: string;
  balance: number;
  accountNumber: string;
  currency: string;
  description: string;
}

export interface Category {
  id: number;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  icon: string;
  color: string;
}

export interface Budget {
  id: number;
  amount: number;
  periodStart: string;
  periodEnd: string;
  periodType: string;
  spentAmount: number;
  remainingAmount: number;
  percentUsed: number;
  isActive: boolean;
  createdAt: string;
  categoryId: number;
  categoryName: string;
  categoryIcon: string;
  categoryColor: string;
}

export interface BudgetRequest {
  amount: number;
  periodStart: string;
  periodEnd: string;
  periodType: string;
  categoryId: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const authApi = {
  login: (email: string, password: string) =>
    api.post<ApiResponse<AuthResponse>>('/auth/login', { email, password }),
  
  register: (username: string, email: string, password: string) =>
    api.post<ApiResponse<AuthResponse>>('/auth/register', { username, email, password }),
  
  logout: () =>
    api.post('/auth/logout'),
};

export const transactionApi = {
  getAll: (page = 0, size = 20) =>
    api.get<ApiResponse<PagedResponse<Transaction>>>('/transactions', { params: { page, size } }),
  
  getSummary: (startDate: string, endDate: string) =>
    api.get<ApiResponse<TransactionSummary>>('/transactions/summary', { params: { startDate, endDate } }),
  
  create: (data: TransactionRequest) =>
    api.post<ApiResponse<Transaction>>('/transactions', data),
};

export const accountApi = {
  getAll: () =>
    api.get<ApiResponse<Account[]>>('/accounts'),
  
  getTotalBalance: () =>
    api.get<ApiResponse<number>>('/accounts/total-balance'),
};

export const categoryApi = {
  getAll: () =>
    api.get<ApiResponse<Category[]>>('/categories'),
  
  getByType: (type: 'INCOME' | 'EXPENSE') =>
    api.get<ApiResponse<Category[]>>('/categories', { params: { type } }),
};

export const budgetApi = {
  getAll: () =>
    api.get<ApiResponse<Budget[]>>('/budgets'),
  
  getById: (id: number) =>
    api.get<ApiResponse<Budget>>(`/budgets/${id}`),
  
  getByDateRange: (startDate: string, endDate: string) =>
    api.get<ApiResponse<Budget[]>>('/budgets/by-date', { params: { startDate, endDate } }),
  
  create: (data: BudgetRequest) =>
    api.post<ApiResponse<Budget>>('/budgets', data),
  
  update: (id: number, data: BudgetRequest) =>
    api.put<ApiResponse<Budget>>(`/budgets/${id}`, data),
  
  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/budgets/${id}`),
};

export default api;
