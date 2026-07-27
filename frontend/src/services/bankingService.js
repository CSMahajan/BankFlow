import API from '../api/axios';

export const transferFunds = (payload) => API.post('/payments/transfer', payload);
export const applyLoan = (loanData) => API.post('/loans/apply', loanData);
export const getMyLoans = () => API.get('/loans/my-loans');
export const payLoanEMI = (loanId, payload) => API.post(`/loans/${loanId}/emi`, payload);
