import { useState, useRef, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { User, ChevronDown, Settings, Package, Star, LogOut, UserCircle, Shield, Heart } from 'lucide-react';

import { useAuth } from '../../hooks';
import type { UserData, UserRole } from '../../types/auth';

interface UserDropdownProps {
  isLoggedIn: boolean;
  user: UserData | null;
  onAuthModalOpen: () => void;
}

export const UserDropdown = ({ isLoggedIn, user, onAuthModalOpen }: UserDropdownProps) => {
  const [isUserDropdownOpen, setIsUserDropdownOpen] = useState(false);
  const userRef = useRef<HTMLDivElement>(null);
  const userTimeoutRef = useRef<number | null>(null);
  const { logout, isLoggingOut } = useAuth();

  const handleUserMouseEnter = useCallback(() => {
    if (userTimeoutRef.current) {
      clearTimeout(userTimeoutRef.current);
      userTimeoutRef.current = null;
    }
    setIsUserDropdownOpen(true);
  }, []);

  const handleUserMouseLeave = useCallback(() => {
    userTimeoutRef.current = setTimeout(() => {
      setIsUserDropdownOpen(false);
    }, 100);
  }, []);

  useEffect(() => {
    return () => {
      if (userTimeoutRef.current) {
        clearTimeout(userTimeoutRef.current);
      }
    };
  }, []);

  const handleLogout = useCallback(async () => {
    setIsUserDropdownOpen(false);
    await logout();
  }, [logout]);

  if (isLoggedIn) {
    return (
      <div className="relative" ref={userRef}>
        <button
          onMouseEnter={handleUserMouseEnter}
          onMouseLeave={handleUserMouseLeave}
          className="p-2 transition-colors hover:text-blue-400 md:cursor-pointer inline-flex items-center space-x-1 group"
          style={{ color: '#d1d5db' }}
        >
          <User className="w-6 h-6 group-hover:text-blue-400 transition-colors" />
          <span className="hidden lg:block text-sm group-hover:text-blue-400 transition-colors">
            {user?.name || user?.firstName || 'Account'}
          </span>
          <ChevronDown className={`w-4 h-4 group-hover:text-blue-400 transition-all duration-200 ${isUserDropdownOpen ? 'rotate-180' : ''}`} />
        </button>

        {isUserDropdownOpen && (
          <div
            className="absolute right-0 mt-2 w-64 card border border-gray-700 shadow-lg z-50 animate-fade-in"
            onMouseEnter={handleUserMouseEnter}
            onMouseLeave={handleUserMouseLeave}
          >
            <div className="p-4 border-b border-gray-700">
              <div className="flex items-center space-x-2">
                <User className="w-8 h-8 text-blue-400" />
                <div>
                  <p className="text-white font-medium">{user?.name || user?.firstName || 'User'}</p>
                  <p className="text-gray-400 text-sm">{user?.email}</p>
                </div>
              </div>
            </div>

            <div className="py-2">
              <Link
                to="/account"
                className="flex items-center space-x-3 px-4 py-3 text-gray-300 hover:text-white hover:bg-gray-700 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                <Settings className="w-5 h-5" />
                <span>My Account</span>
              </Link>

              {user && (user as { role?: UserRole }).role === 'ADMIN' && (
                <Link
                  to="/admin"
                  className="flex items-center space-x-3 px-4 py-3 text-blue-400 hover:text-blue-300 hover:bg-gray-700 transition-colors"
                  onClick={() => setIsUserDropdownOpen(false)}
                >
                  <Shield className="w-5 h-5" />
                  <span>Admin Panel</span>
                </Link>
              )}

              <Link
                to="/orders"
                className="flex items-center space-x-3 px-4 py-3 text-gray-300 hover:text-white hover:bg-gray-700 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                <Package className="w-5 h-5" />
                <span>Orders</span>
              </Link>

              <Link
                to="/reviews"
                className="flex items-center space-x-3 px-4 py-3 text-gray-300 hover:text-white hover:bg-gray-700 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                <Star className="w-5 h-5" />
                <span>Reviews</span>
              </Link>

              <Link
                to="/wishlist"
                className="flex items-center space-x-3 px-4 py-3 text-gray-300 hover:text-white hover:bg-gray-700 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                <Heart className="w-5 h-5" />
                <span>Wishlist</span>
              </Link>
            </div>

            <div className="border-t border-gray-700 py-2">
              <button
                onClick={handleLogout}
                disabled={isLoggingOut}
                className={`flex items-center space-x-3 px-4 py-3 w-full text-left transition-colors cursor-pointer ${
                  isLoggingOut ? 'opacity-50 cursor-not-allowed text-gray-500' : 'text-gray-300 hover:text-red-400 hover:bg-gray-700'
                }`}
              >
                {isLoggingOut ? (
                  <svg className="animate-spin w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                ) : (
                  <LogOut className="w-5 h-5" />
                )}
                <span>{isLoggingOut ? 'Logging out...' : 'Sign Out'}</span>
              </button>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="relative" ref={userRef}>
      <button
        onMouseEnter={handleUserMouseEnter}
        onMouseLeave={handleUserMouseLeave}
        className="p-2 transition-colors hover:text-blue-400 md:cursor-pointer inline-flex items-center space-x-1 group"
        style={{ color: '#d1d5db' }}
      >
        <User className="w-6 h-6 group-hover:text-blue-400 transition-colors" />
        <span className="hidden lg:block text-sm group-hover:text-blue-400 transition-colors">Sign In</span>
        <ChevronDown className={`w-4 h-4 group-hover:text-blue-400 transition-all duration-200 ${isUserDropdownOpen ? 'rotate-180' : ''}`} />
      </button>

      {isUserDropdownOpen && (
        <div
          className="absolute right-0 mt-2 w-64 card border border-gray-700 shadow-lg z-50 animate-fade-in"
          onMouseEnter={handleUserMouseEnter}
          onMouseLeave={handleUserMouseLeave}
        >
          <div className="p-4 border-b border-gray-700">
            <div className="flex items-center space-x-2">
              <UserCircle className="w-8 h-8 text-gray-400" />
              <div>
                <p className="text-white font-medium">Welcome!</p>
                <p className="text-gray-400 text-sm">Sign in to access your account</p>
              </div>
            </div>
          </div>

          <div className="py-2">
            <button
              onClick={() => {
                onAuthModalOpen();
                setIsUserDropdownOpen(false);
              }}
              className="flex items-center space-x-3 px-4 py-3 w-full text-left text-gray-300 hover:text-white hover:bg-gray-700 transition-colors"
            >
              <User className="w-5 h-5" />
              <span>Sign In / Create Account</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
