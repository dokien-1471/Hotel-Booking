import axios, { AxiosError } from "axios";
import { toast } from "sonner";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8081/api";

export const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000, // 10 second timeout
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('Making request to:', config.url, 'with data:', config.data);
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    console.log('Response from:', response.config.url, ':', response.data);
    return response;
  },
  async (error: AxiosError) => {
    console.error('Response error:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });

    // Handle 401 Unauthorized or 403 Forbidden
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Check if this is a public route
      const publicRoutes = ['/rooms', '/auth/login', '/auth/register'];
      const isPublicRoute = publicRoutes.some(route => error.config?.url?.includes(route));

      if (!isPublicRoute) {
        // Only redirect if it's not a public route
        localStorage.removeItem("token");
        window.location.href = "/login";
        toast.error("Vui lòng đăng nhập để tiếp tục.");
      }
    }

    // Handle other errors
    const message = (error.response?.data as any)?.message ||
      (error.response?.data as any)?.error ||
      "Có lỗi xảy ra. Vui lòng thử lại sau.";
    toast.error(message);

    return Promise.reject(error);
  }
); 