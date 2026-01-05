import { useState } from 'react';
import { Package, Clock, CheckCircle, XCircle, Eye, Truck, Search, MapPin, CreditCard, Calendar, Receipt, Zap } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { ordersAPI } from '../lib/api/orders';
import { useIsAuthenticated } from '../hooks';
import { Pagination } from '../components/Pagination';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { Order, OrderFilters } from '../types/order';

const Orders = () => {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<OrderFilters>({});
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isOrderDetailOpen, setIsOrderDetailOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const isAuthenticated = useIsAuthenticated();

  const {
    data: ordersData,
    isLoading: loading,
    error: queryError,
  } = useQuery({
    queryKey: ['orders', filters],
    queryFn: () => ordersAPI.getUserOrders(filters),
    staleTime: 30000,
  });

  const {
    data: stats = null,
  } = useQuery({
    queryKey: ['order-stats'],
    queryFn: ordersAPI.getOrderStats,
    staleTime: 60000,
  });

  const cancelOrderMutation = useMutation({
    mutationFn: (orderId: string) => ordersAPI.cancelOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['order-stats'] });
      toast.success('Order cancelled successfully');
    },
    onError: (error) => {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to cancel order'
        : 'Failed to cancel order';
      toast.error(message);
    },
  });

  const orders = ordersData?.orders || [];
  const totalElements = ordersData?.totalElements || 0;
  const totalPages = ordersData?.totalPages || 0;
  const currentPage = ordersData?.currentPage || 1;
  const error = queryError ? 'Failed to load orders' : null;
  
  const updateFilters = (newFilters: OrderFilters) => {
    setFilters({ ...filters, ...newFilters });
  };
  
  const cancelOrder = (orderId: string) => {
    return cancelOrderMutation.mutateAsync(orderId);
  };

  const getStatusIcon = (status: string) => {
    const upperStatus = status.toUpperCase();
    switch (upperStatus) {
      case 'PENDING':
        return <Clock className="w-5 h-5 text-yellow-400" />;
      case 'PAID':
        return <CheckCircle className="w-5 h-5 text-green-400" />;
      case 'PROCESSING':
        return <Package className="w-5 h-5 text-blue-400" />;
      case 'SHIPPED':
        return <Truck className="w-5 h-5 text-purple-400" />;
      case 'DELIVERED':
        return <CheckCircle className="w-5 h-5 text-green-400" />;
      case 'CANCELLED':
        return <XCircle className="w-5 h-5 text-red-400" />;
      default:
        return <Package className="w-5 h-5 text-gray-400" />;
    }
  };

  const getStatusColor = (status: string) => {
    const upperStatus = status.toUpperCase();
    switch (upperStatus) {
      case 'PENDING':
        return 'text-yellow-400 bg-yellow-900/20 border-yellow-500/30';
      case 'PAID':
        return 'text-green-400 bg-green-900/20 border-green-500/30';
      case 'PROCESSING':
        return 'text-blue-400 bg-blue-900/20 border-blue-500/30';
      case 'SHIPPED':
        return 'text-purple-400 bg-purple-900/20 border-purple-500/30';
      case 'DELIVERED':
        return 'text-green-400 bg-green-900/20 border-green-500/30';
      case 'CANCELLED':
        return 'text-red-400 bg-red-900/20 border-red-500/30';
      default:
        return 'text-gray-400 bg-gray-900/20 border-gray-500/30';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const handleViewOrder = (order: Order) => {
    setSelectedOrder(order);
    setIsOrderDetailOpen(true);
  };

  const handleCancelOrder = async (orderId: string) => {
    if (window.confirm('Are you sure you want to cancel this order?')) {
      await cancelOrder(orderId);
    }
  };

  const handleFilterChange = (status: string) => {
    setStatusFilter(status);
    updateFilters({ status: status || undefined, page: 1 });
  };

  const filteredOrders = orders.filter(order => {
    if (searchQuery) {
      return order.orderNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
             order.items.some(item => 
               item.product.name.toLowerCase().includes(searchQuery.toLowerCase())
             );
    }
    return true;
  });

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400">You need to be logged in to view your orders.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return <LoadingSpinner size="lg" message="Loading your orders..." fullScreen />;
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Error Loading Orders</h1>
          <p className="text-red-400">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-8">
            <h1 className="text-3xl font-bold text-white mb-4 md:mb-0">My Orders</h1>
            <div className="text-gray-400">
              {totalElements} order{totalElements !== 1 ? 's' : ''}
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-4 mb-8">
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-blue-400">{stats?.totalOrders ?? 0}</div>
              <div className="text-gray-400 text-sm">Total</div>
            </div>
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-yellow-400">{stats?.pendingCount ?? 0}</div>
              <div className="text-gray-400 text-sm">Pending</div>
            </div>
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-green-400">{stats?.paidCount ?? 0}</div>
              <div className="text-gray-400 text-sm">Paid</div>
            </div>
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-blue-400">{stats?.processingCount ?? 0}</div>
              <div className="text-gray-400 text-sm">Processing</div>
            </div>
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-purple-400">{stats?.shippedCount ?? 0}</div>
              <div className="text-gray-400 text-sm">Shipped</div>
            </div>
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-green-400">{stats?.deliveredCount ?? 0}</div>
              <div className="text-gray-400 text-sm">Delivered</div>
            </div>
            <div className="card p-4 text-center">
              <div className="text-2xl font-bold text-white">${stats?.totalSpent.toFixed(2) ?? '0.00'}</div>
              <div className="text-gray-400 text-sm">Total Spent</div>
            </div>
          </div>

          <div className="card p-6 mb-8">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type="text"
                    placeholder="Search by order number or product..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>
              <div className="md:w-48">
                <select
                  value={statusFilter}
                  onChange={(e) => handleFilterChange(e.target.value)}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500 cursor-pointer"
                >
                  <option value="">All Orders</option>
                  <option value="PENDING">Pending</option>
                  <option value="PAID">Paid</option>
                  <option value="PROCESSING">Processing</option>
                  <option value="SHIPPED">Shipped</option>
                  <option value="DELIVERED">Delivered</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>
            </div>
          </div>

          {filteredOrders.length === 0 ? (
            <div className="card p-8 text-center">
              <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-white mb-2">No Orders Found</h2>
              <p className="text-gray-400">
                {orders.length === 0 
                  ? "You haven't placed any orders yet. Start shopping to see your orders here!"
                  : "No orders match your current filters."
                }
              </p>
            </div>
          ) : (
            <div className="space-y-6">
              {filteredOrders.map((order) => (
                <div key={order.id} className="card p-6">
                  <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between mb-4">
                    <div className="flex items-center space-x-4 mb-4 lg:mb-0">
                      {getStatusIcon(order.status)}
                      <div>
                        <h3 className="text-lg font-semibold text-white">
                          Order #{order.orderNumber}
                        </h3>
                        <p className="text-gray-400 text-sm">
                          Placed on {formatDate(order.createdAt)}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center space-x-4">
                      <span
                        className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(order.status)}`}
                      >
                        {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                      </span>
                      <span className="text-xl font-bold text-white">
                        ${order.totalAmount.toFixed(2)}
                      </span>
                    </div>
                  </div>

                  <div className="mb-4">
                    <div className="flex items-center space-x-4 overflow-x-auto pb-2">
                      {order.items.slice(0, 3).map((item) => (
                        <div key={item.id} className="flex items-center space-x-3 shrink-0">
                          <img
                            src={item.product.imageUrl}
                            alt={item.product.name}
                            className="w-12 h-12 object-cover rounded-lg"
                          />
                          <div>
                            <p className="text-white text-sm font-medium truncate max-w-32">
                              {item.product.name}
                            </p>
                            <p className="text-gray-400 text-xs">
                              Qty: {item.quantity} × ${item.price.toFixed(2)}
                            </p>
                          </div>
                        </div>
                      ))}
                      {order.items.length > 3 && (
                        <div className="text-gray-400 text-sm shrink-0">
                          +{order.items.length - 3} more item{order.items.length - 3 !== 1 ? 's' : ''}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-col sm:flex-row gap-3">
                    <button
                      onClick={() => handleViewOrder(order)}
                      className="flex items-center justify-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                    >
                      <Eye className="w-4 h-4" />
                      <span>View Details</span>
                    </button>

                    {(order.status.toUpperCase() === 'PENDING' || order.status.toUpperCase() === 'PAID') && (
                      <button
                        onClick={() => handleCancelOrder(order.id)}
                        className="flex items-center justify-center space-x-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                      >
                        <XCircle className="w-4 h-4" />
                        <span>Cancel Order</span>
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="mt-8">
              <Pagination
                currentPage={currentPage + 1}
                totalPages={totalPages}
                onPageChange={(page) => updateFilters({ page })}
              />
            </div>
          )}
        </div>
      </div>

      {isOrderDetailOpen && selectedOrder && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-gray-900 rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-linear-to-r from-blue-600 to-purple-600 p-6 flex items-center justify-between rounded-t-2xl">
              <div className="flex items-center space-x-3">
                <div className="bg-white/20 p-2 rounded-lg">
                  <Receipt className="w-6 h-6 text-white" />
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-white">Order #{selectedOrder.orderNumber}</h2>
                  <p className="text-white/80 text-sm">Placed on {formatDate(selectedOrder.createdAt)}</p>
                </div>
              </div>
              <button
                onClick={() => setIsOrderDetailOpen(false)}
                className="p-2 hover:bg-white/20 rounded-lg transition-colors cursor-pointer"
              >
                <XCircle className="w-6 h-6 text-white" />
              </button>
            </div>
            
            <div className="p-6">
              <div className="mb-8">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    {getStatusIcon(selectedOrder.status)}
                    <span className={`px-4 py-2 rounded-full text-sm font-semibold border ${getStatusColor(selectedOrder.status)}`}>
                      {selectedOrder.status.charAt(0).toUpperCase() + selectedOrder.status.slice(1)}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2 text-gray-400">
                    <Calendar className="w-4 h-4" />
                    <span className="text-sm">Last updated: {formatDate(selectedOrder.updatedAt)}</span>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                <div className="card p-5 border border-gray-700">
                  <div className="flex items-center space-x-3 mb-4">
                    <div className="bg-blue-500/20 p-2 rounded-lg">
                      <MapPin className="w-5 h-5 text-blue-400" />
                    </div>
                    <h4 className="text-lg font-semibold text-white">Shipping Address</h4>
                  </div>
                  <div className="text-gray-300 whitespace-pre-line text-sm leading-relaxed">
                    {selectedOrder.shippingAddress}
                  </div>
                </div>

                <div className="card p-5 border border-gray-700">
                  <div className="flex items-center space-x-3 mb-4">
                    <div className="bg-purple-500/20 p-2 rounded-lg">
                      {selectedOrder.shippingMethod === 'overnight' ? (
                        <Zap className="w-5 h-5 text-purple-400" />
                      ) : selectedOrder.shippingMethod === 'express' ? (
                        <Clock className="w-5 h-5 text-purple-400" />
                      ) : (
                        <Truck className="w-5 h-5 text-purple-400" />
                      )}
                    </div>
                    <h4 className="text-lg font-semibold text-white">Delivery Method</h4>
                  </div>
                  <div className="space-y-2">
                    <p className="text-white font-medium capitalize">
                      {selectedOrder.shippingMethod ? `${selectedOrder.shippingMethod} Shipping` : 'Standard Shipping'}
                    </p>
                    <p className="text-gray-400 text-sm">
                      {selectedOrder.shippingMethod === 'overnight' && 'Delivered within 1 business day'}
                      {selectedOrder.shippingMethod === 'express' && 'Delivered within 2-3 business days'}
                      {(!selectedOrder.shippingMethod || selectedOrder.shippingMethod === 'standard') && 'Delivered within 5-7 business days'}
                    </p>
                  </div>
                </div>
              </div>

              <div className="card p-5 border border-gray-700 mb-6">
                <div className="flex items-center space-x-3 mb-4">
                  <div className="bg-green-500/20 p-2 rounded-lg">
                    <Package className="w-5 h-5 text-green-400" />
                  </div>
                  <h4 className="text-lg font-semibold text-white">
                    Order Items ({selectedOrder.items.length} {selectedOrder.items.length === 1 ? 'item' : 'items'})
                  </h4>
                </div>
                <div className="space-y-4">
                  {selectedOrder.items.map((item) => (
                    <div key={item.id} className="flex items-center space-x-4 py-4 border-b border-gray-700 last:border-b-0 last:pb-0">
                      <img
                        src={item.product.imageUrl}
                        alt={item.product.name}
                        className="w-20 h-20 object-cover rounded-xl shadow-lg"
                      />
                      <div className="flex-1 min-w-0">
                        <h5 className="text-white font-medium text-lg">{item.product.name}</h5>
                        <div className="flex items-center space-x-4 mt-1">
                          <span className="text-gray-400 text-sm">Qty: {item.quantity}</span>
                          <span className="text-gray-500">•</span>
                          <span className="text-gray-400 text-sm">${item.price.toFixed(2)} each</span>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-white font-semibold text-lg">${(item.price * item.quantity).toFixed(2)}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="card p-5 border border-gray-700 bg-linear-to-br from-gray-800/50 to-gray-900/50">
                <div className="flex items-center space-x-3 mb-4">
                  <div className="bg-yellow-500/20 p-2 rounded-lg">
                    <CreditCard className="w-5 h-5 text-yellow-400" />
                  </div>
                  <h4 className="text-lg font-semibold text-white">Payment Summary</h4>
                </div>
                <div className="space-y-3">
                  <div className="flex justify-between text-gray-300">
                    <span>Subtotal ({(() => {
                      const count = selectedOrder.items.reduce((sum, item) => sum + item.quantity, 0);
                      return `${count} ${count === 1 ? 'item' : 'items'}`;
                    })()})</span>
                    <span>${selectedOrder.items.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-gray-300">
                    <span className="flex items-center space-x-2">
                      <span>Shipping</span>
                      {selectedOrder.shippingMethod && (
                        <span className="text-xs text-gray-500 capitalize">({selectedOrder.shippingMethod})</span>
                      )}
                    </span>
                    <span className={selectedOrder.shippingCost === 0 ? 'text-green-400' : ''}>
                      {selectedOrder.shippingCost === 0 ? 'FREE' : `$${(selectedOrder.shippingCost ?? 0).toFixed(2)}`}
                    </span>
                  </div>
                  <div className="border-t border-gray-600 pt-3 mt-3">
                    <div className="flex justify-between text-white font-bold text-xl">
                      <span>Total Paid</span>
                      <span className="text-green-400">${selectedOrder.totalAmount.toFixed(2)}</span>
                    </div>
                  </div>
                </div>
              </div>

              {(selectedOrder.status.toUpperCase() === 'PENDING' || selectedOrder.status.toUpperCase() === 'PAID') && (
                <div className="mt-6 flex justify-end">
                  <button
                    onClick={() => {
                      handleCancelOrder(selectedOrder.id);
                      setIsOrderDetailOpen(false);
                    }}
                    className="flex items-center space-x-2 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                  >
                    <XCircle className="w-5 h-5" />
                    <span>Cancel Order</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Orders;
