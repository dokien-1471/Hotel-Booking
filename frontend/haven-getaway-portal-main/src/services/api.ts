import axios from 'axios';

// Configure base URL - adjust this to match your Spring Boot backend
const API_BASE_URL = 'http://localhost:8080/api';

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
    return Promise.reject(error);
  }
);

// Add response interceptor for better error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials: { email: string; password: string }) =>
    api.post('/auth/login', credentials),
  register: (userData: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    role?: string;
  }) =>
    api.post('/auth/register', userData),
};

// Room API
export const roomAPI = {
  getAllRooms: () => api.get('/rooms'),
  getRoomById: (id: number) => api.get(`/rooms/${id}`),
  getAvailableRooms: () => api.get('/rooms/available'),
  getRoomsByType: (roomType: string) => api.get(`/rooms/type/${roomType}`),
  createRoom: (roomData: any) => api.post('/rooms', roomData),
  updateRoom: (id: number, roomData: any) => api.put(`/rooms/${id}`, roomData),
  deleteRoom: (id: number) => api.delete(`/rooms/${id}`),
};

// Booking API
export const bookingAPI = {
  createBooking: (bookingData: any) => api.post('/bookings', bookingData),
  getBookingById: (id: number) => api.get(`/bookings/${id}`),
  getAllBookings: () => api.get('/bookings'),
  getBookingsByUserId: (userId: number) => api.get(`/bookings/user/${userId}`),
  getBookingsByRoomId: (roomId: number) => api.get(`/bookings/room/${roomId}`),
  getBookingsByStatus: (status: string) => api.get(`/bookings/status/${status}`),
  getBookingsInDateRange: (startDate: string, endDate: string) =>
    api.get('/bookings/date-range', { params: { startDate, endDate } }),
  updateBookingStatus: (id: number, status: string) =>
    api.patch(`/bookings/${id}/status?status=${status}`),
  updateBooking: (id: number, bookingData: any) => api.put(`/bookings/${id}`, bookingData),
  deleteBooking: (id: number) => api.delete(`/bookings/${id}`),
  getBookingByReference: (reference: string) => api.get(`/bookings/reference/${reference}`),
  initiatePayment: (id: number, paymentMethod: string) =>
    api.post(`/bookings/${id}/payment?paymentMethod=${paymentMethod}`),
  cancelBooking: (id: number) => api.post(`/bookings/${id}/cancel`),
};

// Payment API
export const paymentAPI = {
  createPayment: (paymentData: any) => api.post('/payments', paymentData),
  getPaymentStatus: (id: number) => api.get(`/payments/${id}/status`),
};

// VNPay API
export const vnpayAPI = {
  createPayment: (paymentData: any) => api.post('/vnpay/create-payment', paymentData),
  handleCallback: (params: any) => api.get('/vnpay/callback', { params }),
};

// User API
export const userAPI = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (userData: any) => api.put('/users/profile', userData),
};

// File Upload API
export const fileAPI = {
  uploadFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
