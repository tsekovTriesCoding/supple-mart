import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Users, Search, Shield, User, Calendar, Mail, UserCheck } from 'lucide-react';

import { Pagination } from '../../components/Pagination';
import { adminAPI } from '../../lib/api/admin';

const AdminUsers = () => {
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('all');

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users', currentPage, searchQuery, roleFilter],
    queryFn: async () => {
      const response = await adminAPI.getAllUsers({
        page: currentPage,
        limit: 10,
        search: searchQuery || undefined,
        role: roleFilter === 'all' ? undefined : roleFilter,
      });
      return response;
    },
  });

  const { data: statsData } = useQuery({
    queryKey: ['admin-users-stats'],
    queryFn: async () => {
      const [allUsers, admins, customers] = await Promise.all([
        adminAPI.getAllUsers({ page: 1, limit: 1 }),
        adminAPI.getAllUsers({ page: 1, limit: 1, role: 'ADMIN' }),
        adminAPI.getAllUsers({ page: 1, limit: 1, role: 'CUSTOMER' }),
      ]);
      return {
        totalUsers: allUsers.totalElements || 0,
        totalAdmins: admins.totalElements || 0,
        totalCustomers: customers.totalElements || 0,
      };
    },
  });

  const users = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  const totalAdmins = statsData?.totalAdmins || 0;
  const totalCustomers = statsData?.totalCustomers || 0;
  const totalUsers = statsData?.totalUsers || 0;

  const getRoleBadgeColor = (role: string) => {
    return role === 'ADMIN' 
      ? 'bg-purple-900/30 text-purple-400' 
      : 'bg-blue-900/30 text-blue-400';
  };

  const getRoleIcon = (role: string) => {
    return role === 'ADMIN' ? <Shield size={14} /> : <User size={14} />;
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

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-white">Users Management</h2>
          <p className="text-gray-400 mt-1">Manage and monitor user accounts</p>
        </div>
        <div className="flex items-center space-x-2 text-gray-400">
          <Users size={20} />
          <span className="text-sm">Total: {totalUsers} users</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="card p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-400 text-sm">Total Users</p>
              <p className="text-2xl font-bold text-white mt-1">{totalUsers}</p>
            </div>
            <div className="p-3 bg-blue-900/20 rounded-lg">
              <Users className="text-blue-400" size={24} />
            </div>
          </div>
        </div>

        <div className="card p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-400 text-sm">Customers</p>
              <p className="text-2xl font-bold text-white mt-1">{totalCustomers}</p>
            </div>
            <div className="p-3 bg-green-900/20 rounded-lg">
              <UserCheck className="text-green-400" size={24} />
            </div>
          </div>
        </div>

        <div className="card p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-400 text-sm">Administrators</p>
              <p className="text-2xl font-bold text-white mt-1">{totalAdmins}</p>
            </div>
            <div className="p-3 bg-purple-900/20 rounded-lg">
              <Shield className="text-purple-400" size={24} />
            </div>
          </div>
        </div>
      </div>

      <div className="card p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" />
              <input
                type="text"
                placeholder="Search by name or email..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  setCurrentPage(1);
                }}
                className="input w-full"
                style={{ paddingLeft: '3rem', paddingRight: '1rem' }}
              />
            </div>
          </div>

          <select
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value);
              setCurrentPage(1);
            }}
            className="input"
          >
            <option value="all">All Roles</option>
            <option value="CUSTOMER">Customers</option>
            <option value="ADMIN">Administrators</option>
          </select>

          <button
            onClick={() => queryClient.invalidateQueries({ queryKey: ['admin-users'] })}
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
                <th className="px-6 py-4 font-medium">User</th>
                <th className="px-6 py-4 font-medium">Email</th>
                <th className="px-6 py-4 font-medium">Role</th>
                <th className="px-6 py-4 font-medium">Joined</th>
                <th className="px-6 py-4 font-medium">Last Login</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-gray-400">
                    Loading users...
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-gray-400">
                    No users found
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-800/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-full bg-linear-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-semibold">
                          {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                        </div>
                        <div>
                          <p className="text-white font-medium">
                            {user.firstName} {user.lastName}
                          </p>
                          <p className="text-xs text-gray-500">ID: {user.id.substring(0, 8)}...</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-2 text-gray-300">
                        <Mail size={14} className="text-gray-500" />
                        <span>{user.email}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center space-x-1 px-3 py-1 text-xs rounded-full ${getRoleBadgeColor(user.role)}`}>
                        {getRoleIcon(user.role)}
                        <span>{user.role}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-2 text-sm text-gray-400">
                        <Calendar size={14} />
                        <span>{formatDate(user.createdAt)}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-sm text-gray-400">
                        {user.lastLogin ? formatDate(user.lastLogin) : 'Never'}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-4 border-t border-gray-700">
            <div className="text-sm text-gray-400">
              Showing {(currentPage - 1) * 10 + 1} to {Math.min(currentPage * 10, totalElements)} of {totalElements} users
            </div>
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminUsers;
