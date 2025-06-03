import React, { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Eye, EyeOff } from 'lucide-react';
import { authAPI } from '@/services/api';
import { useToast } from '@/hooks/use-toast';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  mode: 'login' | 'register';
  onModeChange: (mode: 'login' | 'register') => void;
}

const AuthModal = ({ isOpen, onClose, mode, onModeChange }: AuthModalProps) => {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    phoneNumber: ''
  });
  const { toast } = useToast();

  const validatePassword = (password: string) => {
    const hasNumber = /\d/.test(password);
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasSpecial = /[@#$%^&+=]/.test(password);
    const isLongEnough = password.length >= 6;

    if (!isLongEnough) return "Password must be at least 6 characters long";
    if (!hasNumber) return "Password must contain at least one number";
    if (!hasUpper) return "Password must contain at least one uppercase letter";
    if (!hasLower) return "Password must contain at least one lowercase letter";
    if (!hasSpecial) return "Password must contain at least one special character";
    return null;
  };

  const validatePhoneNumber = (phone: string) => {
    const phoneRegex = /^\+?[0-9]{10,15}$/;
    if (phone && !phoneRegex.test(phone)) {
      return "Invalid phone number format";
    }
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      if (mode === 'register') {
        // Validate passwords match
        if (formData.password !== formData.confirmPassword) {
          toast({
            title: "Error",
            description: "Passwords don't match",
            variant: "destructive"
          });
          setIsLoading(false);
          return;
        }

        // Validate password strength
        const passwordError = validatePassword(formData.password);
        if (passwordError) {
          toast({
            title: "Error",
            description: passwordError,
            variant: "destructive"
          });
          setIsLoading(false);
          return;
        }

        // Validate phone number if provided
        const phoneError = validatePhoneNumber(formData.phoneNumber);
        if (phoneError) {
          toast({
            title: "Error",
            description: phoneError,
            variant: "destructive"
          });
          setIsLoading(false);
          return;
        }

        // Register user
        const response = await authAPI.register({
          email: formData.email,
          password: formData.password,
          firstName: formData.firstName,
          lastName: formData.lastName,
          phoneNumber: formData.phoneNumber || undefined
        });

        toast({
          title: "Success",
          description: "Account created successfully! Please sign in.",
        });

        // Switch to login mode after successful registration
        onModeChange('login');
        setFormData({ email: formData.email, password: '', confirmPassword: '', firstName: '', lastName: '', phoneNumber: '' });
      } else {
        // Login user
        const response = await authAPI.login({
          email: formData.email,
          password: formData.password
        });

        // Store auth token if provided
        if (response.data.token) {
          localStorage.setItem('token', response.data.token);
          localStorage.setItem('user', JSON.stringify(response.data.user));
        }

        toast({
          title: "Success",
          description: "Logged in successfully!",
        });

        // Close modal and reset form
        onClose();
        setFormData({ email: '', password: '', confirmPassword: '', firstName: '', lastName: '', phoneNumber: '' });
      }
    } catch (error: any) {
      let errorMessage = `Failed to ${mode === 'login' ? 'sign in' : 'create account'}`;

      if (error.code === 'ERR_NETWORK') {
        errorMessage = 'Cannot connect to server. Please make sure your backend is running on http://localhost:8080';
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }

      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive"
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleModalClose = () => {
    onClose();
    setFormData({ email: '', password: '', confirmPassword: '', firstName: '', lastName: '', phoneNumber: '' });
    setShowPassword(false);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleModalClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold text-center">
            {mode === 'login' ? 'Welcome Back' : 'Create Account'}
          </DialogTitle>
        </DialogHeader>

        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4">
          <div className="flex">
            <div className="ml-3">
              <p className="text-sm text-yellow-700">
                <strong>Note:</strong> Make sure your Spring Boot backend is running on http://localhost:8080
              </p>
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          {mode === 'register' && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    type="text"
                    value={formData.firstName}
                    onChange={(e) => handleInputChange('firstName', e.target.value)}
                    placeholder="Enter your first name"
                    required
                    disabled={isLoading}
                    minLength={2}
                    maxLength={50}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    type="text"
                    value={formData.lastName}
                    onChange={(e) => handleInputChange('lastName', e.target.value)}
                    placeholder="Enter your last name"
                    required
                    disabled={isLoading}
                    minLength={2}
                    maxLength={50}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="phoneNumber">Phone Number (Optional)</Label>
                <Input
                  id="phoneNumber"
                  type="tel"
                  value={formData.phoneNumber}
                  onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                  placeholder="Enter your phone number"
                  disabled={isLoading}
                />
              </div>
            </>
          )}

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              placeholder="Enter your email"
              required
              disabled={isLoading}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={formData.password}
                onChange={(e) => handleInputChange('password', e.target.value)}
                placeholder="Enter your password"
                required
                disabled={isLoading}
                minLength={6}
              />
              <button
                type="button"
                className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                onClick={() => setShowPassword(!showPassword)}
                disabled={isLoading}
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {mode === 'register' && (
              <p className="text-xs text-gray-500">
                Password must contain at least 6 characters, one number, one uppercase letter, one lowercase letter, and one special character.
              </p>
            )}
          </div>

          {mode === 'register' && (
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm Password</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
                placeholder="Confirm your password"
                required
                disabled={isLoading}
                minLength={6}
              />
            </div>
          )}

          <Button
            type="submit"
            className="w-full bg-blue-600 hover:bg-blue-700"
            disabled={isLoading}
          >
            {isLoading ? 'Please wait...' : (mode === 'login' ? 'Sign In' : 'Create Account')}
          </Button>
        </form>

        <div className="text-center mt-4">
          <p className="text-sm text-gray-600">
            {mode === 'login' ? "Don't have an account?" : "Already have an account?"}
            <button
              type="button"
              className="text-blue-600 p-0 ml-1 hover:underline"
              onClick={() => onModeChange(mode === 'login' ? 'register' : 'login')}
              disabled={isLoading}
            >
              {mode === 'login' ? 'Sign up' : 'Sign in'}
            </button>
          </p>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default AuthModal;
