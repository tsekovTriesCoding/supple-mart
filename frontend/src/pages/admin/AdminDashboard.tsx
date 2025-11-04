import { BarChart3, Package, ShoppingBag, Users, AlertTriangle } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';

import { adminAPI, type DashboardStats } from '../../lib/api/admin';
import type { ApiError } from '../../types/error';

const AdminDashboard = () => {
  const location = useLocation();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await adminAPI.getDashboardStats();
      setStats(data);
    } catch (err) {
      const apiError = err as ApiError;
      setError(apiError.response?.data?.message || 'Failed to load dashboard stats');
    } finally {
      setIsLoading(false);
    }
  };

  const navItems = [
    { path: '/admin', label: 'Dashboard', icon: BarChart3 },
    { path: '/admin/products', label: 'Products', icon: Package },
    { path: '/admin/orders', label: 'Orders', icon: ShoppingBag },
    { path: '/admin/users', label: 'Users', icon: Users },
  ];

  const isActiveRoute = (path: string) => {
    if (path === '/admin') {
      return location.pathname === '/admin';
    }
    return location.pathname.startsWith(path);
  };

  if (location.pathname !== '/admin') {
    return (
      <div className="min-h-screen bg-gray-950">
        <div className="flex">
          <aside className="w-64 min-h-screen bg-gray-900 border-r border-gray-800">
            <div className="p-6">
              <h1 className="text-2xl font-bold text-white mb-8">Admin Panel</h1>
              <nav className="space-y-2">
                {navItems.map((item) => {
                  const Icon = item.icon;
                  return (
                    <Link
                      key={item.path}
                      to={item.path}
                      className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors ${
                        isActiveRoute(item.path)
                          ? 'bg-blue-600 text-white'
                          : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                      }`}
                    >
                      <Icon size={20} />
                      <span>{item.label}</span>
                    </Link>
                  );
                })}
              </nav>
            </div>
          </aside>

          <main className="flex-1 p-8">
            <Outlet />
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-950">
      <div className="flex">
        <aside className="w-64 min-h-screen bg-gray-900 border-r border-gray-800">
          <div className="p-6">
            <h1 className="text-2xl font-bold text-white mb-8">Admin Panel</h1>
            <nav className="space-y-2">
              {navItems.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors ${
                      isActiveRoute(item.path)
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                    }`}
                  >
                    <Icon size={20} />
                    <span>{item.label}</span>
                  </Link>
                );
              })}
            </nav>
          </div>
        </aside>

        <main className="flex-1 p-8">
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-white mb-2">Dashboard</h2>
            <p className="text-gray-400">Welcome to your admin panel</p>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-900/20 border border-red-900 rounded-lg text-red-400">
              {error}
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
                  <h3 className="text-gray-400 text-sm font-medium">Total Customers</h3>
                  <Users className="text-yellow-400" size={24} />
                </div>
                <p className="text-3xl font-bold text-white">{stats.totalCustomers}</p>
              </div>

              {stats.lowStockProducts > 0 && (
                <div className="card p-6 bg-red-900/10 border-red-900 col-span-full">
                  <div className="flex items-center space-x-3">
                    <AlertTriangle className="text-red-400" size={24} />
                    <div>
                      <h3 className="text-red-400 font-semibold">Low Stock Alert</h3>
                      <p className="text-gray-400">
                        {stats.lowStockProducts} product{stats.lowStockProducts > 1 ? 's' : ''} running low on stock
                      </p>
                    </div>
                    <Link to="/admin/products" className="ml-auto btn-primary">
                      View Products
                    </Link>
                  </div>
                </div>
              )}
            </div>
          ) : null}

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
        </main>
      </div>
    </div>
  );
};

export default AdminDashboard;
