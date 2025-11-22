import { useState } from 'react';
import { Package, Clock, CheckCircle, XCircle, Eye, Truck, Search } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import { ordersAPI } from '../lib/api/orders';
import { Pagination } from '../components/Pagination';
import type { Order, OrderFilters } from '../types/order';

const Orders = () => {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<OrderFilters>({});
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isOrderDetailOpen, setIsOrderDetailOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

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
      try {
        await cancelOrder(orderId);
      } catch (error) {
        console.error('Failed to cancel order:', error);
      }
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

  if (!localStorage.getItem('token')) {
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
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-400">Loading your orders...</p>
        </div>
      </div>
    );
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
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
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
                      className="flex items-center justify-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                      <Eye className="w-4 h-4" />
                      <span>View Details</span>
                    </button>

                    {(order.status.toUpperCase() === 'PENDING' || order.status.toUpperCase() === 'PAID') && (
                      <button
                        onClick={() => handleCancelOrder(order.id)}
                        className="flex items-center justify-center space-x-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
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
            <div className="sticky top-0 bg-gray-900/95 backdrop-blur-sm border-b border-gray-700 p-6 flex items-center justify-between">
              <h2 className="text-2xl font-bold text-white">Order Details</h2>
              <button
                onClick={() => setIsOrderDetailOpen(false)}
                className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
              >
                <XCircle className="w-6 h-6 text-gray-400" />
              </button>
            </div>
            
            <div className="p-6">
              <div className="mb-6">
                <div className="flex items-center space-x-4 mb-4">
                  {getStatusIcon(selectedOrder.status)}
                  <div>
                    <h3 className="text-xl font-semibold text-white">
                      Order #{selectedOrder.orderNumber}
                    </h3>
                    <p className="text-gray-400">
                      Placed on {formatDate(selectedOrder.createdAt)}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(selectedOrder.status)}`}
                  >
                    {selectedOrder.status.charAt(0).toUpperCase() + selectedOrder.status.slice(1)}
                  </span>
                </div>
              </div>

              <div className="card p-4 mb-6">
                <h4 className="text-lg font-semibold text-white mb-3">Shipping Information</h4>
                <div>
                  <p className="text-gray-400 text-sm mb-2">Shipping Address</p>
                  <div className="text-white whitespace-pre-line">
                    {selectedOrder.shippingAddress}
                  </div>
                </div>
              </div>

              <div className="card p-4 mb-6">
                <h4 className="text-lg font-semibold text-white mb-3">Order Items</h4>
                <div className="space-y-4">
                  {selectedOrder.items.map((item) => (
                    <div key={item.id} className="flex items-center space-x-4 py-3 border-b border-gray-700 last:border-b-0">
                      <img
                        src={item.product.imageUrl}
                        alt={item.product.name}
                        className="w-16 h-16 object-cover rounded-lg"
                      />
                      <div className="flex-1">
                        <h5 className="text-white font-medium">{item.product.name}</h5>
                        <p className="text-gray-400 text-sm">Quantity: {item.quantity}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-white font-semibold">${item.price.toFixed(2)}</p>
                        <p className="text-gray-400 text-sm">each</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="card p-4">
                <h4 className="text-lg font-semibold text-white mb-3">Order Summary</h4>
                <div className="space-y-2">
                  <div className="flex justify-between text-white font-semibold text-lg pt-2 border-t border-gray-700">
                    <span>Total:</span>
                    <span>${selectedOrder.totalAmount.toFixed(2)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Orders;
