import { Menu, X, ChevronDown, ShoppingCart, Plus, Minus, ShoppingBag } from 'lucide-react';
import { useState, useRef, useCallback, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';

import { SearchInput } from './SearchInput';
import { UserDropdown } from './UserDropdown';
import { MobileNav } from './MobileNav';
import AuthModal from '../AuthModal';
import { useCart, formatCartPrice, useAuth } from '../../hooks';
import { useProductCategories } from '../../hooks/useProducts';
import { formatCategoryForDisplay, formatCategoryForUrl } from '../../utils/categoryUtils';

const Logo = () => (
  <Link to="/" className="flex items-center space-x-2">
    <img src="/favicon.svg" alt="SuppleMart logo" className="w-8 h-8" />
    <span className="text-xl font-bold text-white">SuppleMart</span>
  </Link>
);

const CategoryNavigation = () => {
  const { data: categoriesData, isLoading } = useProductCategories();
  const [isOpen, setIsOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="relative">
        <button className="nav-link flex items-center space-x-1 font-medium cursor-pointer">
          <span>Categories</span>
          <ChevronDown className="w-4 h-4" />
        </button>
      </div>
    );
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`nav-link flex items-center space-x-1 font-medium cursor-pointer ${isOpen ? 'text-blue-300' : ''}`}
      >
        <span>Categories</span>
        <ChevronDown className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-2 w-64 bg-gray-800 border border-gray-700 rounded-lg shadow-xl z-50">
          <div className="p-2">
            <Link
              to="/products"
              className="block px-3 py-2 text-gray-300 hover:text-white hover:bg-gray-700 rounded-md transition-colors"
              onClick={() => setIsOpen(false)}
            >
              All Products
            </Link>
            {categoriesData?.map((category: string) => (
              <Link
                key={category}
                to={`/products?category=${formatCategoryForUrl(category)}`}
                className="block px-3 py-2 text-gray-300 hover:text-white hover:bg-gray-700 rounded-md transition-colors"
                onClick={() => setIsOpen(false)}
              >
                {formatCategoryForDisplay(category)}
              </Link>
            ))}
          </div>
        </div>
      )}

      {isOpen && (
        <div
          className="fixed inset-0 z-40"
          onClick={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

const navLinks = [
  { path: '/', label: 'Home' },
  { path: '/products', label: 'Products' },
  { path: '/about', label: 'About' },
  { path: '/contact', label: 'Contact' },
];

const DesktopNav = () => {
  const location = useLocation();
  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="hidden md:flex items-center space-x-8">
      {navLinks.map((link) => (
        <Link
          key={link.path}
          to={link.path}
          className={`${isActive(link.path) ? 'nav-link-active' : 'nav-link'} font-medium`}
        >
          {link.label}
        </Link>
      ))}
      <CategoryNavigation />
    </nav>
  );
};

interface CartDropdownProps {
  isOpen: boolean;
  onClose: () => void;
  onMouseEnter?: () => void;
  onMouseLeave?: () => void;
}

const CartDropdown = ({ isOpen, onClose, onMouseEnter, onMouseLeave }: CartDropdownProps) => {
  const { items, updateQuantity, removeItem, totalItems, totalPrice, isLoading, error } = useCart();

  if (!isOpen) return null;

  return (
    <div 
      className="absolute right-0 top-full mt-2 w-96 bg-gray-900 border border-gray-700 rounded-xl shadow-2xl z-50 animate-fade-in"
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <div className="flex items-center justify-between p-4 border-b border-gray-700">
        <div className="flex items-center space-x-2">
          <h3 className="text-lg font-semibold text-white">Shopping Cart</h3>
          {isLoading && (
            <div className="animate-spin rounded-full h-4 w-4 border-2 border-blue-400 border-t-transparent"></div>
          )}
        </div>
        <button
          onClick={onClose}
          className="p-1 hover:bg-gray-800 rounded-lg transition-colors cursor-pointer"
        >
          <X className="w-5 h-5 text-gray-400" />
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-900/50 border-b border-gray-700">
          <p className="text-red-400 text-sm">{error}</p>
        </div>
      )}

      <div className="max-h-96 overflow-y-auto">
        {items.length === 0 ? (
          <div className="p-6 text-center">
            <ShoppingBag className="w-12 h-12 text-gray-600 mx-auto mb-3" />
            <p className="text-gray-400 mb-4">Your cart is empty</p>
            <Link
              to="/products"
              onClick={onClose}
              className="btn-primary text-sm"
            >
              Continue Shopping
            </Link>
          </div>
        ) : (
          <div className={`p-4 space-y-4 ${isLoading ? 'opacity-70 pointer-events-none' : ''}`}>
            {items.map((item) => (
              <div key={item.id} className="flex items-center space-x-3 bg-gray-800 p-3 rounded-lg">
                <img
                  src={item.productImageUrl}
                  alt={item.productName}
                  className="w-12 h-12 object-cover rounded-lg"
                />
                <div className="flex-1 min-w-0">
                  <h4 className="text-white font-medium text-sm truncate">{item.productName}</h4>
                  <p className="text-gray-400 text-sm">{formatCartPrice(item.price)}</p>
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    disabled={isLoading}
                    className="p-1 hover:bg-gray-700 rounded-full transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <Minus className="w-3 h-3 text-gray-400" />
                  </button>
                  <span className="text-white text-sm w-8 text-center">{item.quantity}</span>
                  <button
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    disabled={isLoading}
                    className="p-1 hover:bg-gray-700 rounded-full transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <Plus className="w-3 h-3 text-gray-400" />
                  </button>
                </div>
                <button
                  onClick={() => removeItem(item.id)}
                  disabled={isLoading}
                  className="p-1 hover:bg-red-900 rounded-full transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <X className="w-4 h-4 text-red-400" />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {items.length > 0 && (
        <div className="border-t border-gray-700 p-4">
          <div className="flex items-center justify-between mb-4">
            <span className="text-gray-300">Total ({totalItems} items)</span>
            <span className="text-xl font-bold text-white">{formatCartPrice(totalPrice)}</span>
          </div>
          <div className="space-y-2">
            <Link
              to="/cart"
              onClick={onClose}
              className="btn-secondary w-full text-center block"
            >
              View Cart
            </Link>
            <Link
              to="/checkout"
              onClick={onClose}
              className="btn-primary w-full text-center block"
            >
              Checkout
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};

const CartButton = () => {
  const [isCartDropdownOpen, setIsCartDropdownOpen] = useState(false);
  const cartRef = useRef<HTMLDivElement>(null);
  const cartTimeoutRef = useRef<number | null>(null);
  const { totalItems } = useCart();

  useEffect(() => {
    return () => {
      if (cartTimeoutRef.current) {
        clearTimeout(cartTimeoutRef.current);
      }
    };
  }, []);

  const handleCartMouseEnter = useCallback(() => {
    if (cartTimeoutRef.current) {
      clearTimeout(cartTimeoutRef.current);
      cartTimeoutRef.current = null;
    }
    setIsCartDropdownOpen(true);
  }, []);

  const handleCartMouseLeave = useCallback(() => {
    cartTimeoutRef.current = setTimeout(() => {
      setIsCartDropdownOpen(false);
    }, 100);
  }, []);

  return (
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
  );
};

const Header = () => {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const { isLoggedIn, user, checkAuth } = useAuth();

  // Re-check auth when location changes (e.g., after login redirect)
  useEffect(() => {
    checkAuth();
  }, [location, checkAuth]);

  const handleOpenAuthModal = useCallback(() => {
    setIsAuthModalOpen(true);
  }, []);

  const handleCloseAuthModal = useCallback(() => {
    setIsAuthModalOpen(false);
  }, []);

  const handleAuthSuccess = useCallback(() => {
    setIsAuthModalOpen(false);
    setIsMobileMenuOpen(false);
  }, []);

  const handleCloseMobileMenu = useCallback(() => {
    setIsMobileMenuOpen(false);
  }, []);

  const handleToggleMobileMenu = useCallback(() => {
    setIsMobileMenuOpen(prev => !prev);
  }, []);

  return (
    <header className="border-b sticky top-0 z-50" style={{ backgroundColor: '#111827', borderColor: '#1f2937' }}>
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Logo />
          
          <DesktopNav />
          
          <div className="hidden md:flex items-center space-x-4">
            <SearchInput />
          </div>
          
          <div className="flex items-center space-x-4">
            <CartButton />

            <UserDropdown
              isLoggedIn={isLoggedIn}
              user={user}
              onAuthModalOpen={handleOpenAuthModal}
            />

            <button
              className="md:hidden p-2 transition-colors hover:text-blue-400 cursor-pointer"
              style={{ color: '#d1d5db' }}
              onClick={handleToggleMobileMenu}
            >
              {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        <MobileNav
          isOpen={isMobileMenuOpen}
          onClose={handleCloseMobileMenu}
          isLoggedIn={isLoggedIn}
          user={user}
          onAuthModalOpen={handleOpenAuthModal}
        />
      </div>

      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={handleCloseAuthModal}
        onSuccess={handleAuthSuccess}
      />
    </header>
  );
};

export default Header;
