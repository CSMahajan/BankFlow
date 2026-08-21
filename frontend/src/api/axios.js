import axios from "axios";
const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api/v1",
});
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
API.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    const isLogoutRequest =
      originalRequest.url.includes("/auth/logout");

    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !isLogoutRequest
    ) {
      originalRequest._retry = true;
      try {
        const refreshToken =
          localStorage.getItem("refreshToken");
        if (!refreshToken) {
          throw error;
        }
        const response =
          await axios.post(
            `${API.defaults.baseURL}/auth/refresh`,
            {
              refreshToken,
            }
          );
        const { accessToken, refreshToken: newRefreshToken } = response.data;
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", newRefreshToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return API(originalRequest);
      } catch (refreshError) {
        localStorage.clear();
        window.location.href = "/";
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
export default API;
