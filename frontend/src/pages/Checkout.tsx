import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Elements } from '@stripe/react-stripe-js';
import { ArrowLeft, Package, MapPin, AlertCircle, CheckCircle, ShoppingBag } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { useCart, formatCartPrice } from '../hooks';
import { ordersAPI } from '../lib/api/orders';
import { paymentsAPI } from '../lib/api/payments';
import PaymentForm from '../components/PaymentForm';
import { getStripe } from '../lib/stripe';

const Checkout = () => {
  const navigate = useNavigate();
  const { items, totalPrice, refreshCart } = useCart();

  const [orderSuccess, setOrderSuccess] = useState(false);
  const [shippingAddress, setShippingAddress] = useState({
    fullName: '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: '',
    phone: ''
  });

  const createPaymentMutation = useMutation({
    mutationFn: async (currency: string) => {
      const order = await ordersAPI.createOrder({
        shippingAddress: formatAddressString()
      });
      return paymentsAPI.createPaymentIntent({
        orderId: order.id,
        currency
      });
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to initialize payment'
        : 'Failed to initialize payment';
      toast.error(message);
    },
  });

  const isLoggedIn = !!localStorage.getItem('token');
  const stripePromise = getStripe();
  const clientSecret = createPaymentMutation.data?.clientSecret || null;
  const error = createPaymentMutation.error ? 
    ((createPaymentMutation.error as { response?: { data?: { message?: string } } }).response?.data?.message || 'Failed to initialize payment') : null;

  if (items.length === 0 && !orderSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Package className="w-24 h-24 text-gray-600 mx-auto mb-6" />
          <h2 className="text-3xl font-bold text-white mb-4">Your cart is empty</h2>
          <p className="text-gray-400 mb-8">Add some items to your cart before checking out.</p>
          <Link to="/products" className="btn-primary">Start Shopping</Link>
        </div>
      </div>
    );
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setShippingAddress(prev => ({ ...prev, [name]: value }));
  };

  const validateForm = () => {
    const { fullName, street, city, state, zipCode, country, phone } = shippingAddress;
    if (!fullName.trim()) return 'Full name is required';
    if (!street.trim()) return 'Street address is required';
    if (!city.trim()) return 'City is required';
    if (!state.trim()) return 'State is required';
    if (!zipCode.trim()) return 'ZIP code is required';
    if (!country.trim()) return 'Country is required';
    if (!phone.trim()) return 'Phone number is required';
    return null;
  };

  const formatAddressString = () => {
    return `${shippingAddress.fullName}\n${shippingAddress.street}\n${shippingAddress.city}, ${shippingAddress.state} ${shippingAddress.zipCode}\n${shippingAddress.country}\nPhone: ${shippingAddress.phone}`;
  };

  const handleCreatePaymentIntent = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isLoggedIn) {
      toast.error('Please login to place an order');
      return;
    }
    const validationError = validateForm();
    if (validationError) {
      toast.error(validationError);
      return;
    }
    
    createPaymentMutation.mutate('usd');
  };

  const handlePaymentSuccess = async () => {
    await refreshCart();
    setOrderSuccess(true);
    setTimeout(() => { navigate('/orders'); }, 3000);
  };

  if (orderSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center max-w-md">
          <div className="mb-6 flex justify-center">
            <div className="w-20 h-20 bg-green-500/20 rounded-full flex items-center justify-center">
              <CheckCircle className="w-12 h-12 text-green-400" />
            </div>
          </div>
          <h2 className="text-3xl font-bold text-white mb-4">Order Placed Successfully!</h2>
          <p className="text-gray-400 mb-8">Thank you for your order. You will receive an email confirmation shortly.</p>
          <p className="text-gray-500 text-sm mb-6">Redirecting to your orders page...</p>
          <Link to="/orders" className="btn-primary">View My Orders</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">Checkout</h1>
              <p className="text-gray-400">Complete your order securely with Stripe</p>
            </div>
            <Link to="/cart" className="flex items-center space-x-2 text-gray-400 hover:text-white transition-colors">
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Cart</span>
            </Link>
          </div>

          {!isLoggedIn && (
            <div className="card p-4 mb-6 border-l-4 border-yellow-500">
              <div className="flex items-start space-x-3">
                <AlertCircle className="w-5 h-5 text-yellow-400 shrink-0 mt-0.5" />
                <div>
                  <p className="text-white font-medium">Login Required</p>
                  <p className="text-gray-400 text-sm">
                    You need to <Link to="/login" className="text-blue-400 hover:text-blue-300">login</Link> or <Link to="/register" className="text-blue-400 hover:text-blue-300">create an account</Link> to place an order.
                  </p>
                </div>
              </div>
            </div>
          )}

          {error && (
            <div className="card p-4 mb-6 border-l-4 border-red-500">
              <div className="flex items-start space-x-3">
                <AlertCircle className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
                <div>
                  <p className="text-white font-medium">Error</p>
                  <p className="text-gray-400 text-sm">{error}</p>
                </div>
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-6">
              <form onSubmit={handleCreatePaymentIntent}>
                <div className="card p-6">
                  <div className="flex items-center space-x-3 mb-6">
                    <MapPin className="w-6 h-6 text-blue-400" />
                    <h2 className="text-xl font-semibold text-white">Shipping Address</h2>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-gray-400 text-sm mb-2">Full Name <span className="text-red-400">*</span></label>
                      <input type="text" name="fullName" value={shippingAddress.fullName} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="John Doe" required />
                    </div>

                    <div>
                      <label className="block text-gray-400 text-sm mb-2">Street Address <span className="text-red-400">*</span></label>
                      <input type="text" name="street" value={shippingAddress.street} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="123 Main Street" required />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-gray-400 text-sm mb-2">City <span className="text-red-400">*</span></label>
                        <input type="text" name="city" value={shippingAddress.city} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="New York" required />
                      </div>
                      <div>
                        <label className="block text-gray-400 text-sm mb-2">State <span className="text-red-400">*</span></label>
                        <input type="text" name="state" value={shippingAddress.state} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="NY" required />
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-gray-400 text-sm mb-2">ZIP Code <span className="text-red-400">*</span></label>
                        <input type="text" name="zipCode" value={shippingAddress.zipCode} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="10001" required />
                      </div>
                      <div>
                        <label className="block text-gray-400 text-sm mb-2">Country <span className="text-red-400">*</span></label>
                        <input type="text" name="country" value={shippingAddress.country} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="United States" required />
                      </div>
                    </div>

                    <div>
                      <label className="block text-gray-400 text-sm mb-2">Phone Number <span className="text-red-400">*</span></label>
                      <input type="tel" name="phone" value={shippingAddress.phone} onChange={handleInputChange} disabled={!!clientSecret} className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 disabled:opacity-50" placeholder="+1 (555) 123-4567" required />
                    </div>
                  </div>

                  {!clientSecret && (
                    <button type="submit" disabled={createPaymentMutation.isPending || !isLoggedIn} className="w-full mt-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-600 disabled:cursor-not-allowed font-medium cursor-pointer">
                      {createPaymentMutation.isPending ? 'Processing...' : 'Continue to Payment'}
                    </button>
                  )}
                </div>
              </form>

              {clientSecret && stripePromise && (
                <Elements stripe={stripePromise} options={{ clientSecret, appearance: { theme: 'night', variables: { colorPrimary: '#3b82f6', colorBackground: '#1f2937', colorText: '#ffffff', colorDanger: '#ef4444', borderRadius: '0.5rem' } } }}>
                  <div className="mt-6">
                    <PaymentForm onPaymentSuccess={handlePaymentSuccess} />
                  </div>
                </Elements>
              )}
            </div>

            <div className="lg:col-span-1">
              <div className="card p-6 sticky top-4">
                <div className="flex items-center space-x-3 mb-4">
                  <ShoppingBag className="w-6 h-6 text-blue-400" />
                  <h2 className="text-xl font-semibold text-white">Order Summary</h2>
                </div>

                <div className="space-y-4 mb-6 max-h-96 overflow-y-auto">
                  {items.map((item) => (
                    <div key={item.id} className="flex items-center space-x-3">
                      <img src={item.productImageUrl} alt={item.productName} className="w-12 h-12 object-cover rounded-lg" />
                      <div className="flex-1 min-w-0">
                        <p className="text-white text-sm font-medium truncate">{item.productName}</p>
                        <p className="text-gray-400 text-xs">Qty: {item.quantity}  {formatCartPrice(item.price)}</p>
                      </div>
                      <span className="text-white font-medium text-sm">{formatCartPrice(item.price * item.quantity)}</span>
                    </div>
                  ))}
                </div>

                <div className="border-t border-gray-700 pt-4 space-y-3">
                  <div className="flex justify-between text-gray-400">
                    <span>Subtotal:</span>
                    <span>{formatCartPrice(totalPrice)}</span>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Shipping:</span>
                    <span className="text-green-400">FREE</span>
                  </div>
                  <div className="border-t border-gray-700 pt-3 flex justify-between text-white font-semibold text-lg">
                    <span>Total:</span>
                    <span>{formatCartPrice(totalPrice)}</span>
                  </div>
                </div>

                <p className="text-gray-500 text-xs text-center mt-6">By placing your order, you agree to our terms and conditions</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
