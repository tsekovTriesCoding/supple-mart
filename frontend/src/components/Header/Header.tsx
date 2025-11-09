import { Menu, X } from 'lucide-react';
import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

import { Logo } from './Logo';
import { DesktopNav } from './DesktopNav';
import { SearchInput } from './SearchInput';
import { CartButton } from './CartButton';
import { UserDropdown } from './UserDropdown';
import { MobileNav } from './MobileNav';
import AuthModal from '../AuthModal';
import type { UserData } from '../../types/auth';

const Header = () => {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState<UserData | null>(null);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

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
    };

    checkAuth();
  }, [location]);

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
              onAuthModalOpen={() => setIsAuthModalOpen(true)}
            />

            <button
              className="md:hidden p-2 transition-colors hover:text-blue-400"
              style={{ color: '#d1d5db' }}
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        <MobileNav
          isOpen={isMobileMenuOpen}
          onClose={() => setIsMobileMenuOpen(false)}
          isLoggedIn={isLoggedIn}
          user={user}
          onAuthModalOpen={() => setIsAuthModalOpen(true)}
        />
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
  );
};

export default Header;
