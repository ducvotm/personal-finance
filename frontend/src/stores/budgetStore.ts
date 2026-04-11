import { create } from 'zustand';
import { budgetApi, Budget, BudgetRequest } from '../api/client';

interface BudgetState {
  budgets: Budget[];
  isLoading: boolean;
  error: string | null;
  fetchBudgets: () => Promise<void>;
  createBudget: (data: BudgetRequest) => Promise<Budget>;
  updateBudget: (id: number, data: BudgetRequest) => Promise<Budget>;
  deleteBudget: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useBudgetStore = create<BudgetState>((set, get) => ({
  budgets: [],
  isLoading: false,
  error: null,

  fetchBudgets: async () => {
    set({ isLoading: true, error: null });
    try {
      const response = await budgetApi.getAll();
      set({ budgets: response.data.data, isLoading: false });
    } catch (error: any) {
      set({ error: error.response?.data?.message || 'Failed to fetch budgets', isLoading: false });
    }
  },

  createBudget: async (data: BudgetRequest) => {
    set({ isLoading: true, error: null });
    try {
      const response = await budgetApi.create(data);
      const newBudget = response.data.data;
      set({ budgets: [...get().budgets, newBudget], isLoading: false });
      return newBudget;
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create budget';
      set({ error: message, isLoading: false });
      throw new Error(message);
    }
  },

  updateBudget: async (id: number, data: BudgetRequest) => {
    set({ isLoading: true, error: null });
    try {
      const response = await budgetApi.update(id, data);
      const updatedBudget = response.data.data;
      set({
        budgets: get().budgets.map((b) => (b.id === id ? updatedBudget : b)),
        isLoading: false,
      });
      return updatedBudget;
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to update budget';
      set({ error: message, isLoading: false });
      throw new Error(message);
    }
  },

  deleteBudget: async (id: number) => {
    set({ isLoading: true, error: null });
    try {
      await budgetApi.delete(id);
      set({ budgets: get().budgets.filter((b) => b.id !== id), isLoading: false });
    } catch (error: any) {
      set({ error: error.response?.data?.message || 'Failed to delete budget', isLoading: false });
      throw new Error(error.response?.data?.message || 'Failed to delete budget');
    }
  },

  clearError: () => set({ error: null }),
}));