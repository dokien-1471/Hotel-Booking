import axios from 'axios';

// Configure base URL - adjust this to match your Spring Boot backend
const API_BASE_URL = '/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Add response interceptor for better error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear token and redirect to home page on unauthorized
      localStorage.removeItem('token');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// Remove duplicate API endpoints since we're using authService
export const roomAPI = {
  getAllRooms: () => api.get('/rooms'),
  getRoomById: (id: number) => api.get(`/rooms/${id}`),
  searchRooms: (params: { checkIn: string; checkOut: string; guests: number }) =>
    api.get('/rooms/search', { params }),
};

export const bookingAPI = {
  createBooking: (bookingData: any) => api.post('/bookings', bookingData),
  getUserBookings: () => api.get('/bookings/user'),
  getBookingById: (id: number) => api.get(`/bookings/${id}`),
  cancelBooking: (id: number) => api.delete(`/bookings/${id}`),
};

export const paymentAPI = {
  createPayment: (paymentData: any) => api.post('/payments', paymentData),
  getPaymentStatus: (id: number) => api.get(`/payments/${id}/status`),
};

export const vnpayAPI = {
  createPayment: (paymentData: any) => api.post('/vnpay/create-payment', paymentData),
  handleCallback: (params: any) => api.get('/vnpay/callback', { params }),
};

export const userAPI = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (userData: any) => api.put('/users/profile', userData),
};

export const fileAPI = {
  uploadFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
