import { Link, useLocation, useNavigate } from 'react-router-dom';
import { User, Settings, Package, Star, LogOut } from 'lucide-react';
import { useState } from 'react';

import { authAPI } from '../../lib/api';
import { useCart } from '../../hooks/useCart';
import { useProductCategories } from '../../hooks/useProducts';
import { SearchInput } from './SearchInput';
import { formatCategoryForDisplay, formatCategoryForUrl } from '../../utils/categoryUtils';
import type { UserData } from '../../types/auth';

interface MobileNavProps {
  isOpen: boolean;
  onClose: () => void;
  isLoggedIn: boolean;
  user: UserData | null;
  onAuthModalOpen: () => void;
}

const navLinks = [
  { path: '/', label: 'Home' },
  { path: '/products', label: 'Products' },
  { path: '/about', label: 'About' },
  { path: '/contact', label: 'Contact' },
];

export const MobileNav = ({ isOpen, onClose, isLoggedIn, user, onAuthModalOpen }: MobileNavProps) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const { data: categoriesData } = useProductCategories();
  const { refreshCart } = useCart();

  const isActive = (path: string) => location.pathname === path;

  const handleLogout = async () => {
    if (isLoggingOut) return;

    setIsLoggingOut(true);
    try {
      await authAPI.logout();

      localStorage.removeItem('token');
      localStorage.removeItem('user');

      await refreshCart();
      onClose();
      navigate('/');
      window.location.reload();
    } catch (error) {
      console.error('Logout failed:', error);

      // Even if logout API fails, clear local data
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      await refreshCart();
      onClose();
      navigate('/');
      window.location.reload();
    } finally {
      setIsLoggingOut(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="md:hidden py-4 border-t animate-fade-in" style={{ borderColor: '#1f2937' }}>
      <nav className="flex flex-col space-y-4">
        {navLinks.map((link) => (
          <Link
            key={link.path}
            to={link.path}
            className={`${isActive(link.path) ? 'nav-link-active' : 'nav-link'} font-medium py-2`}
            onClick={onClose}
          >
            {link.label}
          </Link>
        ))}

        <div className="pt-2 border-t border-gray-700">
          <div className="text-gray-400 text-sm font-medium mb-2">Categories</div>
          <div className="pl-4 space-y-2">
            <Link
              to="/products"
              className="block text-gray-300 hover:text-white py-1"
              onClick={onClose}
            >
              All Products
            </Link>
            {categoriesData?.map((category: string) => (
              <Link
                key={category}
                to={`/products?category=${formatCategoryForUrl(category)}`}
                className="block text-gray-300 hover:text-white py-1"
                onClick={onClose}
              >
                {formatCategoryForDisplay(category)}
              </Link>
            ))}
          </div>
        </div>

        {isLoggedIn ? (
          <div className="pt-2 border-t border-gray-700">
            <div className="flex items-center space-x-2 py-2">
              <User className="w-5 h-5 text-gray-400" />
              <span className="text-gray-300">Welcome, {user?.name || user?.firstName || 'User'}</span>
            </div>

            <div className="space-y-1 mb-4">
              <Link
                to="/account"
                className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
                onClick={onClose}
              >
                <Settings className="w-5 h-5" />
                <span>My Account</span>
              </Link>

              <Link
                to="/orders"
                className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
                onClick={onClose}
              >
                <Package className="w-5 h-5" />
                <span>Orders</span>
              </Link>

              <Link
                to="/reviews"
                className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
                onClick={onClose}
              >
                <Star className="w-5 h-5" />
                <span>Reviews</span>
              </Link>
            </div>

            <button
              onClick={() => {
                handleLogout();
              }}
              disabled={isLoggingOut}
              className={`nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left ${
                isLoggingOut ? 'opacity-50 cursor-not-allowed' : 'hover:text-red-400'
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
              <span>{isLoggingOut ? 'Logging out...' : 'Logout'}</span>
            </button>
          </div>
        ) : (
          <button
            onClick={() => {
              onAuthModalOpen();
              onClose();
            }}
            className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
          >
            <User className="w-5 h-5" />
            <span>Sign In</span>
          </button>
        )}

        <div className="pt-2">
          <SearchInput className="w-full" />
        </div>
      </nav>
    </div>
  );
};
