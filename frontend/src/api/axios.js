import axios from 'axios';

// Create a configured Axios instance
const API = axios.create({
  baseURL: '/api/v1', // Proxies cleanly to http://localhost:8080/api/v1 via Vite
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Automatically attaches Bearer token if available
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default API;
