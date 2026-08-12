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

export const fetchMyLoans = async () => {
  const response = await API.get("/loans/my-loans");
  return response.data;
};

export const fetchLoanRepayments = async (loanNumber) => {
  const response = await API.get(
    `/loans/${loanNumber}/repayments`
  );
  return response.data;
};

export const payEmi = async (paymentData) => {
  const response = await API.post('/loans/pay-emi', paymentData);
  return response.data;
};

export const fetchPendingLoans = async ({
  page = 0,
  size = 10,
  search = "",
  loanType = "ALL",
}) => {

  const response = await API.get("/loans/pending", {
    params: {
      page,
      size,
      ...(search && { search }),
      ...(loanType !== "ALL" && { loanType }),
    },
  });

  return response.data;
};

export const fetchLoanSummary = async () => {
  const response = await API.get(
    "/admin/loans/summary"
  );

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

export const fetchUsers = async ({
  page = 0,
  size = 10,
  search = "",
  role = "ALL",
}) => {

  const response = await API.get("/users", {
    params: {
      page,
      size,
      ...(search && { search }),
      ...(role !== "ALL" && { role }),
    },
  });

  return response.data;
};

export const fetchAuditLogs = async ({
  page = 0,
  size = 10,
  search = "",
  role,
  action,
  actions,
}) => {

  const response = await API.get("/admin/audit-logs", {
    params: {
      page,
      size,
      ...(search && { search }),
      ...(role && role !== "ALL" && {
        role,
      }),
      ...(action && action !== "ALL" && {
        action,
      }),
      ...(actions?.length && {
        actions,
      }),
    }, paramsSerializer: { indexes: null, },
  });

  return response.data;
};

export const fetchMyCards = () =>
  API.get("/cards/my-cards")
    .then(res => res.data);

export const toggleCardStatus = async (cardId) => {
  const response = await API.patch(`/cards/${cardId}/toggle-status`);
  return response.data;
};

export const updateCardLimit = async (cardId, newLimit) => {
  const response = await API.patch(
    `/cards/${cardId}/limit`, null, { params: { newLimit, }, });
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

export const getMyTransactions = async (params) => {
  const response = await API.get(
    "/transactions/my-transactions",
    { params }
  );
  return response.data;
};

export const getTransactionDetails = async (transactionId) => {
  const response = await API.get(
    `/transactions/${transactionId}`
  );
  return response.data;
};

export const createScheduledTransfer = async (request) => {
  const response = await API.post(
    "/scheduled-transfers",
    request
  );
  return response.data;
};

export const fetchMyScheduledTransfers = async () => {
  const response = await API.get(
    "/scheduled-transfers/my-transfers"
  );
  return response.data;
};

export const cancelScheduledTransfer = async (transferId) => {
  const response = await API.patch(
    `/scheduled-transfers/${transferId}/cancel`
  );
  return response.data;
};

export const fetchUserDetails = async (userId) => {
  const response = await API.get(
    `/users/${userId}`
  );
  return response.data;
};

export const fetchUserAccounts = async (userId) => {
  const response = await API.get(`/admin/users/${userId}/accounts`);
  return response.data;
};

export const fetchUserCards = async (userId) => {
  const response = await API.get(`/admin/users/${userId}/cards`);
  return response.data;
};

export const fetchUserLoans = async (userId) => {
  const response = await API.get(`/admin/users/${userId}/loans`);
  return response.data;
};

export const fetchUserFixedDeposits = async (userId) => {
  const response = await API.get(`/admin/users/${userId}/fixed-deposits`);
  return response.data;
};

export const fetchAllAccounts = async ({
  page = 0,
  size = 20,
  search = "",
  status = "ALL",
}) => {

  const response = await API.get("/admin/accounts", {
    params: {
      page,
      size,
      ...(search && { search }),
      ...(status !== "ALL" && { status }),
    },
  });

  return response.data;
};

export const fetchAccountTransactions = async (
  accountNumber,
  page = 0,
  size = 10
) => {
  const response = await API.get(
    `/transactions/admin/accounts/${accountNumber}/transactions`,
    {
      params: {
        page,
        size,
      },
    }
  );

  return response.data;
};

export const fetchAccountSummary = async () => {

  const response = await API.get(
    "/admin/accounts/summary"
  );

  return response.data;
};

export const freezeAccount = async (accountNumber) => {
  const response = await API.patch(
    `/admin/accounts/${accountNumber}/freeze`
  );
  return response.data;
};

export const unfreezeAccount = async (accountNumber) => {
  const response = await API.patch(
    `/admin/accounts/${accountNumber}/unfreeze`
  );
  return response.data;
};

export const fetchAllCards = async ({
  page = 0,
  size = 20,
  search = "",
  status = "ALL",
}) => {

  const response = await API.get("/admin/cards", {
    params: {
      page,
      size,
      ...(search && { search }),
      ...(status !== "ALL" && { status }),
    },
  });

  return response.data;
};

export const fetchCardSummary = async () => {

  const response = await API.get(
    "/admin/cards/summary"
  );

  return response.data;
};

export const blockCard = async (cardId) => {
  const response = await API.patch(
    `/admin/cards/${cardId}/block`
  );
  return response.data;
};

export const unblockCard = async (cardId) => {
  const response = await API.patch(
    `/admin/cards/${cardId}/unblock`
  );
  return response.data;
};

export const fetchMonthlyAnalytics = async () => {
  const response = await API.get("/dashboard/analytics/monthly");
  return response.data;
};

export const exportTransactionsPdf = async (params) => {
  const response = await API.get(
    "/transactions/export/pdf", {
    params, responseType: "blob",
  });

  return response.data;
};

export const exportTransactionsExcel = async (params) => {
  const response = await API.get(
    "/transactions/export/excel", {
    params, responseType: "blob",
  });
  return response.data;
};


export const login = async (credentials) => {
  const response = await API.post("/auth/login", credentials);
  return response.data;
};

export const registerCustomer = async (request) => {
  const response = await API.post("/auth/register", request);
  return response.data;
};

export const registerAdmin = async (request, token) => {
  const response = await API.post(
    "/admin/users/create-admin",
    request,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};

export const verifyEmail = async (token) => {
  const response = await API.get(
    `/auth/verify-email?token=${token}`
  );
  return response.data;
};

export const resendVerificationEmail = async (email) => {
  await API.post("/auth/resend-verification", { email, });
};

export const forgotPassword = async (request) => {
  await API.post("/auth/forgot-password", request);
};

export const resetPassword = async (request) => {
  await API.post("/auth/reset-password", request);
};

export const fetchCurrentUser = async () => {
  const response = await API.get("/users/me");
  return response.data;
};

export const updateProfile = async (request) => {
  await API.put("/users/profile", request);
};

export const changePassword = async (request) => {
  await API.patch("/users/change-password", request);
};