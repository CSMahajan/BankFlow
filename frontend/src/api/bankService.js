import API from './axios';

// Fetch dashboard summary
export const fetchDashboardSummary = async () => {
  const response = await API.get('/dashboard/summary');
  return response.data;
};

// Transfer funds API call (matched to /transfers)
export const transferFunds = async (transferData) => {
  const response = await API.post('/transfers', transferData);
  return response.data;
};

// Create a new bank account API call
export const createBankAccount = async (accountData) => {
  // accountData should include { accountType, initialDeposit, branchName }
  const response = await API.post('/accounts/create', accountData);
  return response.data;
};

export const applyLoan = async (loanData) => {
  const response = await API.post('/loans/apply', loanData);
  return response.data;
};

export const payEmi = async (paymentData) => {
    const response = await API.post('/loans/pay-emi', paymentData);
    return response.data;
};