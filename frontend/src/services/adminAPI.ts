import { api } from './api';
import axios from 'axios';
import { Room, User, CreateRoomData, CreateUserData, UpdateUserData } from '../types/admin';

// Create a separate axios instance for admin APIs
const adminApi = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor
adminApi.interceptors.request.use(
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

// Add response interceptor
adminApi.interceptors.response.use(
  (response) => {
    console.log('Response:', response.data);
    return response;
  },
  (error) => {
    console.error('Response error:', error.response?.data);
    return Promise.reject(error);
  }
);

export const adminRoomAPI = {
  getAllRooms: () => adminApi.get<Room[]>('/rooms'),
  getRoomById: (id: number) => adminApi.get<Room>(`/rooms/${id}`),
  createRoom: (roomData: CreateRoomData) => adminApi.post<Room>('/rooms', roomData),
  updateRoom: (id: number, roomData: CreateRoomData) => adminApi.put<Room>(`/rooms/${id}`, roomData),
  deleteRoom: (id: number) => adminApi.delete(`/rooms/${id}`),
  searchRooms: (query: string) => adminApi.get<Room[]>(`/rooms/search?q=${query}`),
  addRoomImage: (id: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return adminApi.post<Room>(`/rooms/${id}/images`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  removeRoomImage: (id: number, imageUrl: string) => adminApi.delete<Room>(`/rooms/${id}/images`, { params: { imageUrl } }),
};

// User CRUD operations
export const adminUserAPI = {
  getAllUsers: () => adminApi.get<User[]>('/users'),
  getUserById: (id: number) => adminApi.get<User>(`/users/${id}`),
  createUser: (userData: CreateUserData) => adminApi.post<User>('/users', userData),
  updateUser: (id: number, userData: UpdateUserData) => adminApi.put<User>(`/users/${id}`, userData),
  deleteUser: (id: number) => adminApi.delete(`/users/${id}`),
  searchUsers: (query: string) => adminApi.get<User[]>(`/users/search?q=${query}`),
  toggleUserStatus: (id: number) => adminApi.patch<User>(`/users/${id}/toggle-status`),
};


export const adminStatsAPI = {
  getDashboardStats: () => adminApi.get('/stats'),
  getRecentBookings: () => adminApi.get('/bookings/recent'),
};
