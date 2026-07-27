import API from '../api/axios';

export const getMyAccounts = () => API.get('/accounts/my-accounts');
export const createAccount = (accountData) => API.post('/accounts', accountData);
export const getAccountBalance = (accountNo) => API.get(`/accounts/${accountNo}/balance`);
export const getMyCards = () => API.get('/cards/my-cards');
export const toggleCardStatus = (cardId, status) => API.patch(`/cards/${cardId}/status`, { status });
export const toggleCardLimit = (cardId, limit) => API.patch(`/cards/${cardId}/limit`, { limit });
