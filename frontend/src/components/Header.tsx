import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ShoppingCart, User, Search, Menu, X, LogOut, UserCircle, Package, Star, Settings, ChevronDown } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';

import { authAPI } from '../lib/api';
import { useCart } from '../hooks/useCart';
import { useProductCategories } from '../hooks/useProducts';
import CartDropdown from './CartDropdown';
import AuthModal from './AuthModal';
import CategoryNavigation from './CategoryNavigation';
import type { UserData } from '../types/auth';
import { formatCategoryForDisplay, formatCategoryForUrl } from '../utils/categoryUtils';

const Header = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [user, setUser] = useState<UserData | null>(null);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [isCartDropdownOpen, setIsCartDropdownOpen] = useState(false);
  const [isUserDropdownOpen, setIsUserDropdownOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const cartRef = useRef<HTMLDivElement>(null);
  const userRef = useRef<HTMLDivElement>(null);
  const cartTimeoutRef = useRef<number | null>(null);
  const userTimeoutRef = useRef<number | null>(null);
  const { totalItems, refreshCart } = useCart();
  const { data: categoriesData } = useProductCategories();

  const handleCartMouseEnter = () => {
    if (cartTimeoutRef.current) {
      clearTimeout(cartTimeoutRef.current);
      cartTimeoutRef.current = null;
    }
    setIsCartDropdownOpen(true);
  }

  const handleCartMouseLeave = () => {
    cartTimeoutRef.current = setTimeout(() => {
      setIsCartDropdownOpen(false)
    }, 100);
  }

  const handleUserMouseEnter = () => {
    if (userTimeoutRef.current) {
      clearTimeout(userTimeoutRef.current);
      userTimeoutRef.current = null;
    }
    setIsUserDropdownOpen(true);
  }

  const handleUserMouseLeave = () => {
    userTimeoutRef.current = setTimeout(() => {
      setIsUserDropdownOpen(false)
    }, 100);
  }

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('token');
      const userData = localStorage.getItem('user');

      if (token && userData) {
        setIsLoggedIn(true);
        setUser(JSON.parse(userData));
      } else {
        setIsLoggedIn(false);
        setUser(null);
      }
    }

    checkAuth()
  }, [location]);

  useEffect(() => {
    return () => {
      if (cartTimeoutRef.current) {
        clearTimeout(cartTimeoutRef.current)
      }
      if (userTimeoutRef.current) {
        clearTimeout(userTimeoutRef.current)
      }
    }
  }, []);

  const handleLogout = async () => {
    if (isLoggingOut) return;

    setIsLoggingOut(true);
    try {
      await authAPI.logout();

      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      setIsLoggedIn(false);
      setUser(null);

      await refreshCart();

      navigate('/');
    } catch (error) {
      console.error('Logout failed:', error);

      // Even if logout API fails, clear local data
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setIsLoggedIn(false);
      setUser(null);
      
      await refreshCart();
      
      navigate('/');
    } finally {
      setIsLoggingOut(false);
    }
  };

  const isActive = (path: string) => location.pathname === path;

  const navLinks = [
    { path: '/', label: 'Home' },
    { path: '/products', label: 'Products' },
    { path: '/about', label: 'About' },
    { path: '/contact', label: 'Contact' },
  ]

  return (
    <header className="border-b sticky top-0 z-50" style={{ backgroundColor: '#111827', borderColor: '#1f2937' }}>
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ backgroundColor: '#2563eb' }}>
              <span className="text-white font-bold text-xl">S</span>
            </div>
            <span className="text-xl font-bold text-white">SuppleMart</span>
          </Link>
          <nav className="hidden md:flex items-center space-x-8">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={`${isActive(link.path) ? 'nav-link-active' : 'nav-link'
                  } font-medium`}
              >
                {link.label}
              </Link>
            ))}
            <CategoryNavigation />
          </nav>
          <div className="hidden md:flex items-center space-x-4">
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <Search className="w-4 h-4" style={{ color: '#9ca3af' }} />
              </div>
              <input
                type="text"
                placeholder="Search products..."
                className="input w-64"
                style={{ paddingLeft: '2.75rem', paddingRight: '1rem' }}
              />
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <div 
              ref={cartRef}
              className="relative"
              onMouseEnter={handleCartMouseEnter}
              onMouseLeave={handleCartMouseLeave}
            >
              <Link
                to="/cart"
                className="relative p-2 transition-colors md:cursor-pointer hover:text-blue-400 group block"
                style={{ color: '#d1d5db' }}
              >
                <ShoppingCart className="w-6 h-6 group-hover:text-blue-400 transition-colors" />
                {totalItems > 0 && (
                  <span className="absolute -top-1 -right-1 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center" style={{ backgroundColor: '#2563eb' }}>
                    {totalItems > 99 ? '99+' : totalItems}
                  </span>
                )}
              </Link>
              
              <CartDropdown
                isOpen={isCartDropdownOpen}
                onClose={() => setIsCartDropdownOpen(false)}
                onMouseEnter={handleCartMouseEnter}
                onMouseLeave={handleCartMouseLeave}
              />
            </div>

            {isLoggedIn ? (
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
                    </div>
                    
                    <div className="border-t border-gray-700 py-2">
                      <button
                        onClick={() => {
                          handleLogout();
                          setIsUserDropdownOpen(false);
                        }}
                        disabled={isLoggingOut}
                        className={`flex items-center space-x-3 px-4 py-3 w-full text-left transition-colors ${isLoggingOut ? 'opacity-50 cursor-not-allowed text-gray-500' : 'text-gray-300 hover:text-red-400 hover:bg-gray-700'
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
            ) : (
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
                          setIsAuthModalOpen(true);
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
            )}
            <button
              className="md:hidden p-2 transition-colors hover:text-blue-400"
              style={{ color: '#d1d5db' }}
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
        {isMobileMenuOpen && (
          <div className="md:hidden py-4 border-t animate-fade-in" style={{ borderColor: '#1f2937' }}>
            <nav className="flex flex-col space-y-4">
              {navLinks.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`${isActive(link.path) ? 'nav-link-active' : 'nav-link'
                    } font-medium py-2`}
                  onClick={() => setIsMobileMenuOpen(false)}
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
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    All Products
                  </Link>
                  {categoriesData?.map((category: string) => (
                    <Link
                      key={category}
                      to={`/products?category=${formatCategoryForUrl(category)}`}
                      className="block text-gray-300 hover:text-white py-1"
                      onClick={() => setIsMobileMenuOpen(false)}
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
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <Settings className="w-5 h-5" />
                      <span>My Account</span>
                    </Link>
                    
                    <Link
                      to="/orders"
                      className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <Package className="w-5 h-5" />
                      <span>Orders</span>
                    </Link>
                    
                    <Link
                      to="/reviews"
                      className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <Star className="w-5 h-5" />
                      <span>Reviews</span>
                    </Link>
                  </div>
                  
                  <button
                    onClick={() => {
                      handleLogout()
                      setIsMobileMenuOpen(false)
                    }}
                    disabled={isLoggingOut}
                    className={`nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left ${isLoggingOut ? 'opacity-50 cursor-not-allowed' : 'hover:text-red-400'
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
                    setIsAuthModalOpen(true);
                    setIsMobileMenuOpen(false);
                  }}
                  className="nav-link font-medium py-2 inline-flex items-center space-x-2 w-full text-left hover:text-blue-400"
                >
                  <User className="w-5 h-5" />
                  <span>Sign In</span>
                </button>
              )}
              <div className="pt-2">
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                    <Search className="w-4 h-4" style={{ color: '#9ca3af' }} />
                  </div>
                  <input
                    type="text"
                    placeholder="Search products..."
                    className="input w-full"
                    style={{ paddingLeft: '2.75rem', paddingRight: '1rem' }}
                  />
                </div>
              </div>
            </nav>
          </div>
        )}
      </div>

      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={() => {
          setIsAuthModalOpen(false);
          setIsMobileMenuOpen(false);
        }}
      />
    </header>
  )
}

export default Header;
