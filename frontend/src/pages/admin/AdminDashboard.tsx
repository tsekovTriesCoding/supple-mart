import { BarChart3, Package, ShoppingBag, Users, AlertTriangle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';

import { adminAPI } from '../../lib/api/admin';
import type { ApiError } from '../../types/error';

const AdminDashboard = () => {
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['admin-dashboard-stats'],
    queryFn: () => adminAPI.getDashboardStats(),
    staleTime: 30 * 1000,
    refetchOnWindowFocus: true,
  });

  const errorMessage = error 
    ? ((error as ApiError).response?.data?.message || 'Failed to load dashboard stats')
    : null;

  return (
    <>
      <div className="mb-8">
        <h2 className="text-3xl font-bold text-white mb-2">Dashboard</h2>
        <p className="text-gray-400">Welcome to your admin panel</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-red-900/20 border border-red-900 rounded-lg text-red-400">
          {errorMessage}
        </div>
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="card p-6 animate-pulse">
              <div className="h-4 bg-gray-700 rounded w-1/2 mb-4"></div>
              <div className="h-8 bg-gray-700 rounded w-3/4"></div>
            </div>
          ))}
        </div>
      ) : stats ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="card p-6 hover:border-blue-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Total Products</h3>
                <Package className="text-blue-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{stats.totalProducts}</p>
            </div>

            <div className="card p-6 hover:border-green-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Total Orders</h3>
                <ShoppingBag className="text-green-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{stats.totalOrders}</p>
            </div>

            <div className="card p-6 hover:border-purple-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Total Revenue</h3>
                <BarChart3 className="text-purple-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">${stats.totalRevenue.toFixed(2)}</p>
            </div>

            <div className="card p-6 hover:border-yellow-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Total Users</h3>
                <Users className="text-yellow-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{stats.totalCustomers}</p>
            </div>
          </div>

          {stats.lowStockProducts > 0 && (
            <div className="card p-6 bg-red-900/10 border-red-900 mt-6">
              <div className="flex items-center space-x-3">
                <AlertTriangle className="text-red-400" size={24} />
                <div className="flex-1">
                  <h3 className="text-red-400 font-semibold">Low Stock Alert</h3>
                  <p className="text-gray-400">
                    {stats.lowStockProducts} product{stats.lowStockProducts > 1 ? 's' : ''} running low on stock
                  </p>
                </div>
                <Link to="/admin/products" className="btn-primary">
                  View Products
                </Link>
              </div>
            </div>
          )}

          <div className="mt-8">
            <h3 className="text-xl font-bold text-white mb-4">Quick Actions</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Link
                to="/admin/products"
                className="card p-6 hover:border-blue-500 transition-colors text-center"
              >
                <Package className="mx-auto mb-3 text-blue-400" size={32} />
                <h4 className="text-white font-semibold mb-2">Manage Products</h4>
                <p className="text-gray-400 text-sm">Add, edit, or remove products</p>
              </Link>

              <Link
                to="/admin/orders"
                className="card p-6 hover:border-green-500 transition-colors text-center"
              >
                <ShoppingBag className="mx-auto mb-3 text-green-400" size={32} />
                <h4 className="text-white font-semibold mb-2">View Orders</h4>
                <p className="text-gray-400 text-sm">Process and manage orders</p>
              </Link>

              <Link
                to="/admin/users"
                className="card p-6 hover:border-purple-500 transition-colors text-center"
              >
                <Users className="mx-auto mb-3 text-purple-400" size={32} />
                <h4 className="text-white font-semibold mb-2">Manage Users</h4>
                <p className="text-gray-400 text-sm">View and manage customers</p>
              </Link>
            </div>
          </div>
        </>
      ) : null}
    </>
  );
};

export default AdminDashboard;
