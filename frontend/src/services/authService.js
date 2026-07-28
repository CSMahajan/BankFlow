import API from '../api/axios';

export const loginUser = (credentials) =>
  API.post('/auth/login', credentials);

export const registerUser = (userData) =>
  API.post('/auth/register', userData);

export const registerAdmin = (userData, token) =>
  API.post(
    '/admin/users/create-admin',
    userData,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );