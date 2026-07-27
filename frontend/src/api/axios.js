import axios from 'axios';

// Create a configured Axios instance
const API = axios.create({
  baseURL: 'http://localhost:8080/api/v1', // Update port if your Spring Boot app runs elsewhere
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT token if present in localStorage
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
