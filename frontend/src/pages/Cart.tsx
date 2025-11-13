import { Minus, Plus, X, ShoppingBag, ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

import { useCart, formatCartPrice } from '../hooks';

const Cart = () => {
  const { items, updateQuantity, removeItem, clearCart, totalItems, totalPrice } = useCart();

  if (items.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <ShoppingBag className="w-24 h-24 text-gray-600 mx-auto mb-6" />
          <h2 className="text-3xl font-bold text-white mb-4">Your cart is empty</h2>
          <p className="text-gray-400 mb-8 max-w-md">
            Looks like you haven't added any items to your cart yet. 
            Start shopping to fill it up!
          </p>
          <Link to="/products" className="btn-primary">
            Continue Shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Shopping Cart</h1>
          <p className="text-gray-400">{totalItems} {totalItems === 1 ? 'item' : 'items'} in your cart</p>
        </div>
        <Link
          to="/products"
          className="btn-secondary flex items-center space-x-2"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Continue Shopping</span>
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-4">
          <div className="flex justify-end">
            <button
              onClick={clearCart}
              className="text-red-400 hover:text-red-300 text-sm transition-colors"
            >
              Clear All Items
            </button>
          </div>

          {items.map((item) => (
            <div key={item.id} className="bg-gray-800 rounded-lg p-6">
              <div className="flex items-center space-x-4">
                <img
                  src={item.productImageUrl}
                  alt={item.productName}
                  className="w-20 h-20 object-cover rounded-lg"
                />

                <div className="flex-1 min-w-0">
                  <h3 className="text-white font-semibold text-lg mb-1">{item.productName}</h3>
                  <p className="text-gray-400 mb-3">{formatCartPrice(item.price)} each</p>

                  <div className="flex items-center space-x-3">
                    <span className="text-gray-300 text-sm">Quantity:</span>
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => updateQuantity(item.id, item.quantity - 1)}
                        className="p-2 hover:bg-gray-700 rounded-lg transition-colors"
                      >
                        <Minus className="w-4 h-4 text-gray-400" />
                      </button>
                      
                      <span className="text-white font-medium w-12 text-center">{item.quantity}</span>
                      
                      <button
                        onClick={() => updateQuantity(item.id, item.quantity + 1)}
                        className="p-2 hover:bg-gray-700 rounded-lg transition-colors"
                      >
                        <Plus className="w-4 h-4 text-gray-400" />
                      </button>
                    </div>
                  </div>
                </div>

                <div className="flex flex-col items-end space-y-2">
                  <span className="text-white font-semibold text-lg">
                    {formatCartPrice(item.price * item.quantity)}
                  </span>
                  <button
                    onClick={() => removeItem(item.id)}
                    className="p-2 hover:bg-red-900 rounded-lg transition-colors group"
                  >
                    <X className="w-5 h-5 text-red-400 group-hover:text-red-300" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="lg:col-span-1">
          <div className="bg-gray-800 rounded-lg p-6 sticky top-8">
            <h3 className="text-xl font-semibold text-white mb-6">Order Summary</h3>
            
            <div className="space-y-4 mb-6">
              <div className="flex justify-between text-gray-300">
                <span>Subtotal ({totalItems} {totalItems === 1 ? 'item' : 'items'})</span>
                <span>{formatCartPrice(totalPrice)}</span>
              </div>
              
              <div className="flex justify-between text-gray-300">
                <span>Shipping</span>
                <span className="text-green-400">Free</span>
              </div>
              
              <div className="border-t border-gray-700 pt-4">
                <div className="flex justify-between text-white font-semibold text-lg">
                  <span>Total</span>
                  <span>{formatCartPrice(totalPrice)}</span>
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <Link
                to="/checkout"
                className="btn-primary w-full text-center block"
              >
                Proceed to Checkout
              </Link>
              
              <Link
                to="/products"
                className="btn-secondary w-full text-center block"
              >
                Continue Shopping
              </Link>
            </div>

            <div className="mt-6 p-4 bg-gray-700 rounded-lg">
              <p className="text-gray-300 text-sm text-center">
                🔒 Secure checkout with SSL encryption
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
