import API from '../api/axios';

export const loginUser = (credentials) => API.post('/auth/login', credentials);
export const registerUser = (userData) => API.post('/auth/register', userData);
export const loginAdmin = (credentials) => API.post('/admin/login', credentials);
