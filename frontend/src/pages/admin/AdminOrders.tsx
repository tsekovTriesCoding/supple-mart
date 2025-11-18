import { useCallback, useEffect, useReducer } from 'react';
import { Package, Search, Eye, Calendar, DollarSign, User } from 'lucide-react';

import { Pagination } from '../../components/Pagination';
import { adminAPI } from '../../lib/api/admin';
import type { AdminOrder } from '../../types/admin';
import { adminOrdersReducer, ordersInitialState } from '../../reducers/admin';

const AdminOrders = () => {
  const [state, dispatch] = useReducer(adminOrdersReducer, ordersInitialState);

  const statusOptions = ['all', 'pending', 'paid', 'processing', 'shipped', 'delivered', 'cancelled'];

  const loadOrders = useCallback(async () => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      const response = await adminAPI.getAllOrders({
        page: state.currentPage,
        limit: 10,
        status: state.selectedStatus !== 'all' ? state.selectedStatus : undefined,
      });

      dispatch({
        type: 'SET_ORDERS',
        payload: {
          orders: response.content || [],
          totalPages: response.totalPages || 0,
          totalElements: response.totalElements || 0,
        },
      });
    } catch (error) {
      console.error('Failed to load orders:', error);
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, [state.currentPage, state.selectedStatus]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  const handleStatusChange = async (orderId: number, newStatus: string) => {
    try {
      await adminAPI.updateOrderStatus(orderId, newStatus);
      loadOrders();
    } catch (error) {
      console.error('Failed to update order status:', error);
      alert('Failed to update order status');
    }
  };

  const handleViewDetails = (order: AdminOrder) => {
    dispatch({ type: 'OPEN_DETAILS_MODAL', payload: order });
  };

  const getStatusColor = (status: string) => {
    const statusLower = status.toLowerCase();
    switch (statusLower) {
      case 'pending':
        return 'bg-yellow-900/30 text-yellow-400';
      case 'paid':
        return 'bg-emerald-900/30 text-emerald-400';
      case 'processing':
        return 'bg-blue-900/30 text-blue-400';
      case 'shipped':
        return 'bg-purple-900/30 text-purple-400';
      case 'delivered':
        return 'bg-green-900/30 text-green-400';
      case 'cancelled':
        return 'bg-red-900/30 text-red-400';
      default:
        return 'bg-gray-900/30 text-gray-400';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatPrice = (price: number) => {
    return `$${price.toFixed(2)}`;
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-white">Orders Management</h2>
          <p className="text-gray-400 mt-1">Manage and track all customer orders</p>
        </div>
        <div className="flex items-center space-x-2 text-gray-400">
          <Package size={20} />
          <span className="text-sm">Total: {state.totalElements} orders</span>
        </div>
      </div>

      <div className="card p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <select
            value={state.selectedStatus}
            onChange={(e) => {
              dispatch({ type: 'SET_SELECTED_STATUS', payload: e.target.value });
            }}
            className="input"
          >
            {statusOptions.map((status) => (
              <option key={status} value={status}>
                {status.charAt(0).toUpperCase() + status.slice(1)}
              </option>
            ))}
          </select>

          <button
            onClick={loadOrders}
            className="btn-secondary flex items-center space-x-2"
          >
            <Search size={18} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="border-b border-gray-700">
              <tr className="text-left text-gray-400 text-sm">
                <th className="px-6 py-4 font-medium">Order #</th>
                <th className="px-6 py-4 font-medium">Customer</th>
                <th className="px-6 py-4 font-medium">Date</th>
                <th className="px-6 py-4 font-medium">Total</th>
                <th className="px-6 py-4 font-medium">Status</th>
                <th className="px-6 py-4 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {state.loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-400">
                    Loading orders...
                  </td>
                </tr>
              ) : state.orders.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-400">
                    No orders found
                  </td>
                </tr>
              ) : (
                state.orders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-800/50 transition-colors">
                    <td className="px-6 py-4">
                      <span className="font-mono text-sm text-gray-300">{order.orderNumber}</span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col">
                        <span className="text-white">{order.customerName}</span>
                        <span className="text-sm text-gray-400">{order.customerEmail}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-sm text-gray-300">{formatDate(order.createdAt)}</span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-white">{formatPrice(order.totalAmount)}</span>
                    </td>
                    <td className="px-6 py-4">
                      <select
                        value={order.status}
                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                        className={`px-3 py-1 text-xs rounded-full cursor-pointer ${getStatusColor(order.status)}`}
                        style={{ 
                          backgroundColor: 'transparent',
                          border: 'none',
                          appearance: 'none',
                        }}
                      >
                        <option value="PENDING">Pending</option>
                        <option value="PAID">Paid</option>
                        <option value="PROCESSING">Processing</option>
                        <option value="SHIPPED">Shipped</option>
                        <option value="DELIVERED">Delivered</option>
                        <option value="CANCELLED">Cancelled</option>
                      </select>
                    </td>
                    <td className="px-6 py-4">
                      <button
                        onClick={() => handleViewDetails(order)}
                        className="p-2 text-blue-400 hover:bg-blue-900/20 rounded"
                        title="View Details"
                      >
                        <Eye size={18} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {state.totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-4 border-t border-gray-700">
            <div className="text-sm text-gray-400">
              Showing {(state.currentPage - 1) * 10 + 1} to {Math.min(state.currentPage * 10, state.totalElements)} of {state.totalElements} orders
            </div>
            <Pagination
              currentPage={state.currentPage}
              totalPages={state.totalPages}
              onPageChange={(page) => dispatch({ type: 'SET_CURRENT_PAGE', payload: page })}
            />
          </div>
        )}
      </div>

      {state.showDetailsModal && state.selectedOrder && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="card max-w-3xl w-full max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-xl font-bold text-white">Order Details</h3>
              <button
                onClick={() => dispatch({ type: 'CLOSE_DETAILS_MODAL' })}
                className="text-gray-400 hover:text-white"
              >
                ×
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              <div className="bg-gray-800/50 p-4 rounded-lg">
                <div className="flex items-center space-x-2 text-gray-400 mb-2">
                  <Package size={18} />
                  <span className="text-sm">Order Number</span>
                </div>
                <p className="text-white font-mono">{state.selectedOrder.orderNumber}</p>
              </div>

              <div className="bg-gray-800/50 p-4 rounded-lg">
                <div className="flex items-center space-x-2 text-gray-400 mb-2">
                  <Calendar size={18} />
                  <span className="text-sm">Order Date</span>
                </div>
                <p className="text-white">{formatDate(state.selectedOrder.createdAt)}</p>
              </div>

              <div className="bg-gray-800/50 p-4 rounded-lg">
                <div className="flex items-center space-x-2 text-gray-400 mb-2">
                  <User size={18} />
                  <span className="text-sm">Customer</span>
                </div>
                <p className="text-white">{state.selectedOrder.customerName}</p>
                <p className="text-sm text-gray-400">{state.selectedOrder.customerEmail}</p>
              </div>

              <div className="bg-gray-800/50 p-4 rounded-lg">
                <div className="flex items-center space-x-2 text-gray-400 mb-2">
                  <DollarSign size={18} />
                  <span className="text-sm">Total Amount</span>
                </div>
                <p className="text-white text-xl font-bold">{formatPrice(state.selectedOrder.totalAmount)}</p>
              </div>
            </div>

            <div className="mb-6">
              <h4 className="text-lg font-semibold text-white mb-4">Order Items</h4>
              <div className="space-y-3">
                {state.selectedOrder.items.map((item: { id: number; productName: string; quantity: number; price: number }, index: number) => (
                  <div key={index} className="flex items-center justify-between bg-gray-800/50 p-4 rounded-lg">
                    <div className="flex-1">
                      <p className="text-white font-medium">{item.productName}</p>
                      <p className="text-sm text-gray-400">Quantity: {item.quantity}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-white font-semibold">{formatPrice(item.price)}</p>
                      <p className="text-sm text-gray-400">each</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex items-center justify-between pt-4 border-t border-gray-700">
              <span className="text-gray-400">Current Status:</span>
              <span className={`px-4 py-2 rounded-full text-sm font-medium ${getStatusColor(state.selectedOrder.status)}`}>
                {state.selectedOrder.status}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminOrders;
