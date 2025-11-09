import { X, Plus, Minus, ShoppingBag } from 'lucide-react';
import { Link } from 'react-router-dom';

import { useCart, formatCartPrice } from '../../hooks';

interface CartDropdownProps {
  isOpen: boolean;
  onClose: () => void;
  onMouseEnter?: () => void;
  onMouseLeave?: () => void;
};

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
          <h3 className="text-lg font-semibold text-white">Shopping Cart</h3>
          <button
            onClick={onClose}
            className="p-1 hover:bg-gray-800 rounded-lg transition-colors"
          >
            <X className="w-5 h-5 text-gray-400" />
          </button>
        </div>

        {error && (
          <div className="p-4 bg-red-900/50 border-b border-gray-700">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        {isLoading && (
          <div className="p-6 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-400 mx-auto"></div>
            <p className="text-gray-400 mt-2">Updating cart...</p>
          </div>
        )}

        <div className="max-h-96 overflow-y-auto">{!isLoading && (
          <>
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
            <div className="p-4 space-y-4">
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
                      className="p-1 hover:bg-gray-700 rounded-full transition-colors"
                    >
                      <Minus className="w-3 h-3 text-gray-400" />
                    </button>
                    
                    <span className="text-white text-sm w-8 text-center">{item.quantity}</span>
                    
                    <button
                      onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      className="p-1 hover:bg-gray-700 rounded-full transition-colors"
                    >
                      <Plus className="w-3 h-3 text-gray-400" />
                    </button>
                  </div>
                  <button
                    onClick={() => removeItem(item.id)}
                    className="p-1 hover:bg-red-900 rounded-full transition-colors"
                  >
                    <X className="w-4 h-4 text-red-400" />
                  </button>
                </div>
              ))}
            </div>
          )}
          </>
        )}
        </div>
        {!isLoading && items.length > 0 && (
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

export default CartDropdown;
