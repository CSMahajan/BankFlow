import API from '../api/axios';

// Fetch overall balance, total income, and total expenses
export const getDashboardSummary = () => {
  return API.get('/dashboard/summary');
};

// Fetch monthly analytical summary
export const getMonthlySummary = () => {
  return API.get('/dashboard/monthly-summary');
};

// Fetch recent transactions list
export const getRecentTransactions = () => {
  return API.get('/dashboard/transactions');
};
