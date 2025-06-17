import { api } from "@/lib/api";
import type { LoginCredentials, RegisterData, AuthResponse, User } from "@/types/auth";

export const authService = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    console.log('Auth service - Login request:', credentials);
    const response = await api.post<AuthResponse>("/auth/login", credentials);
    console.log('Auth service - Login response:', response.data);
    const { token, user } = response.data;
    localStorage.setItem("token", token);
    return response.data;
  },

  async register(data: RegisterData): Promise<AuthResponse> {
    console.log('Auth service - Register request:', data);
    try {
      const { confirmPassword, ...registerData } = data;
      const response = await api.post<AuthResponse>("/auth/register", registerData);
      console.log('Auth service - Register response:', response.data);
      // Don't store token after registration
      // Let user login after registration
      return response.data;
    } catch (error: any) {
      console.error('Auth service - Register error:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      });

      // Enhance error message
      if (error.response?.data?.message) {
        error.message = error.response.data.message;
      } else if (error.response?.data?.error) {
        error.message = error.response.data.error;
      } else if (!error.message) {
        error.message = 'Đăng ký thất bại. Vui lòng thử lại.';
      }

      throw error;
    }
  },

  async getCurrentUser(): Promise<User> {
    const response = await api.get<User>("/users/profile");
    return response.data;
  },

  logout() {
    localStorage.removeItem("token");
    window.location.href = "/";
  },

  getToken(): string | null {
    return localStorage.getItem("token");
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },
}; 