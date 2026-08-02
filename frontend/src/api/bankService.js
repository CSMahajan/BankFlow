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

export const fetchPendingLoans = async () => {
  const response = await API.get('/loans/pending');
  return response.data;
};

export const approveLoan = async (loanId) => {
  const response = await API.put(`/loans/${loanId}/approve`);
  return response.data;
};

export const rejectLoan = async (loanId, remarks) => {
  const response = await API.put(
    `/loans/${loanId}/reject`,
    { remarks }
  );
  return response.data;
};

export const fetchAdminDashboardSummary = async () => {
  const response = await API.get('/dashboard/admin-summary');
  return response.data;
};

export const fetchUsers = async () => {
  const response = await API.get("/users");
  return response.data;
};

export const fetchAuditLogs = (page = 0, size = 10) =>
  API.get(`/admin/audit-logs?page=${page}&size=${size}`)
    .then(res => res.data);

export const fetchMyCards = () =>
  API.get("/cards/my-cards")
    .then(res => res.data);

export const toggleCardStatus = async (cardId) => {
  const response = await API.patch(`/cards/${cardId}/toggle-status`);
  return response.data;
};

export const updateCardLimit = async (cardId, newLimit) => {
  const response = await API.patch(
    `/cards/${cardId}/limit`,
    null,
    {
      params: {
        newLimit,
      },
    }
  );
  return response.data;
};

export const fetchMyAccounts = async () => {
  const response = await API.get("/accounts/my-accounts");
  return response.data;
};

export const issueCard = async (cardData) => {
  const response = await API.post("/cards/issue", cardData);
  return response.data;
};

export const createFixedDeposit = async (request) => {
  const response = await API.post("/fd/create", request);
  return response.data;
};

export const fetchMyFixedDeposits = async () => {
  const response = await API.get("/fd/my-fds");
  return response.data;
};

export const closeFixedDeposit = async (fdNumber) => {
  const response = await API.patch(`/fd/${fdNumber}/close`);
  return response.data;
};

export const toggleAccountStatus = async (accountNumber) => {
  const response = await API.patch(
    `/accounts/${accountNumber}/toggle-status`
  );
  return response.data;
};

export const getMyTransactions = (params) =>
  API.get("/transactions/my-transactions", {
    params,
  });