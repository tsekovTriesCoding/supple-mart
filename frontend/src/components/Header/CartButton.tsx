import { Link } from 'react-router-dom';
import { ShoppingCart } from 'lucide-react';
import { useRef, useState } from 'react';

import { useCart } from '../../hooks/useCart';
import CartDropdown from '../CartDropdown';

export const CartButton = () => {
  const [isCartDropdownOpen, setIsCartDropdownOpen] = useState(false);
  const cartRef = useRef<HTMLDivElement>(null);
  const cartTimeoutRef = useRef<number | null>(null);
  const { totalItems } = useCart();

  const handleCartMouseEnter = () => {
    if (cartTimeoutRef.current) {
      clearTimeout(cartTimeoutRef.current);
      cartTimeoutRef.current = null;
    }
    setIsCartDropdownOpen(true);
  };

  const handleCartMouseLeave = () => {
    cartTimeoutRef.current = setTimeout(() => {
      setIsCartDropdownOpen(false);
    }, 100);
  };

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
