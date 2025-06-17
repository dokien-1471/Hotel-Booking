import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Menu, X, Hotel, User, LogOut } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/providers/AuthProvider';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const ProfileNavbar = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-white shadow-sm border-b sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <Hotel className="h-8 w-8 text-blue-600" />
            <span className="text-2xl font-bold text-gray-900">Haven</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-8">
            <Link to="/" className="text-gray-700 hover:text-blue-600 transition-colors">Home</Link>
            <a href="/#rooms" className="text-gray-700 hover:text-blue-600 transition-colors">Rooms</a>
            <a href="/#about" className="text-gray-700 hover:text-blue-600 transition-colors">About</a>
            <a href="/#contact" className="text-gray-700 hover:text-blue-600 transition-colors">Contact</a>
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className="text-gray-700 hover:text-blue-600 transition-colors">Admin</Link>
            )}
          </div>

          {/* User Menu */}
          <div className="hidden md:flex items-center space-x-4">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="flex items-center gap-2">
                  <User className="h-5 w-5" />
                  <span>{user?.firstName} {user?.lastName}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild>
                  <Link to="/profile" className="cursor-pointer">
                    Profile
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-red-600">
                  <LogOut className="h-4 w-4 mr-2" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>

          {/* Mobile menu button */}
          <div className="md:hidden">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setIsMenuOpen(!isMenuOpen)}
            >
              {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </Button>
          </div>
        </div>

        {/* Mobile menu */}
        {isMenuOpen && (
          <div className="md:hidden">
            <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t">
              <Link to="/" className="block px-3 py-2 text-gray-700 hover:text-blue-600">Home</Link>
              <a href="/#rooms" className="block px-3 py-2 text-gray-700 hover:text-blue-600">Rooms</a>
              <a href="/#about" className="block px-3 py-2 text-gray-700 hover:text-blue-600">About</a>
              <a href="/#contact" className="block px-3 py-2 text-gray-700 hover:text-blue-600">Contact</a>
              {user?.role === 'ADMIN' && (
                <Link to="/admin" className="block px-3 py-2 text-gray-700 hover:text-blue-600">Admin</Link>
              )}
              <div className="flex flex-col space-y-2 px-3 pt-4">
                <Link to="/profile" className="block px-3 py-2 text-gray-700 hover:text-blue-600">
                  Profile
                </Link>
                <Button
                  variant="outline"
                  onClick={handleLogout}
                  className="w-full text-red-600"
                >
                  <LogOut className="h-4 w-4 mr-2" />
                  Logout
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
};

export default ProfileNavbar; 