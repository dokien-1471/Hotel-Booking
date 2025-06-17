import { createContext, useContext, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/services/auth';
import type { User, LoginCredentials, RegisterData, AuthResponse } from '@/types/auth';
import { useState, useEffect } from 'react';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  login: (credentials: LoginCredentials) => Promise<AuthResponse>;
  register: (data: RegisterData) => Promise<AuthResponse>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const initializeAuth = async () => {
    const storedToken = authService.getToken();
    console.log('Initializing auth with token:', storedToken);

    if (storedToken) {
      try {
        const currentUser = await authService.getCurrentUser();
        console.log('Current user retrieved:', currentUser);
        setUser(currentUser);
        setToken(storedToken);
      } catch (err) {
        console.error('Error getting current user:', err);
        setError('Failed to get user information');
        authService.logout();
      }
    }
    setLoading(false);
  };

  useEffect(() => {
    initializeAuth();
  }, []);

  const login = async (credentials: LoginCredentials) => {
    try {
      console.log('Attempting login with:', credentials.email);
      setLoading(true);
      setError(null);
      const response = await authService.login(credentials);
      console.log('Login successful:', response);
      setUser(response.user);
      setToken(response.token);

      console.log('Login successful, user role:', response.user.role);
      if (response.user.role === 'ROLE_ADMIN') {
        navigate('/admin');
      } else {
        navigate('/profile');
      }
      return response;
    } catch (err) {
      console.error('Login failed:', err);
      setError(err instanceof Error ? err.message : 'Login failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (data: RegisterData) => {
    try {
      console.log('Attempting registration with:', data.email);
      setLoading(true);
      setError(null);
      const response = await authService.register(data);
      console.log('Registration successful:', response);

      // Don't set user and token immediately after registration
      // Wait for login instead
      return response;
    } catch (err) {
      console.error('Registration failed:', err);
      const errorMessage = err.response?.data?.message ||
        err.response?.data?.error ||
        (err instanceof Error ? err.message : 'Đăng ký thất bại');
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    console.log('Logging out user');
    authService.logout();
    setUser(null);
    setToken(null);
    navigate('/');
  };

  const value = {
    user,
    token,
    loading,
    error,
    login,
    register,
    logout,
    isAuthenticated: !!token,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
} 