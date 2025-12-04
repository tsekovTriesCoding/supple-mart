import { Database, Trash2, RefreshCw, Activity, Target, XCircle, Zap } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

import { adminAPI } from '../../lib/api/admin';
import type { CacheStatsResponse } from '../../types/admin';

const AdminCache = () => {
  const queryClient = useQueryClient();
  const [selectedCache, setSelectedCache] = useState<string | null>(null);

  const { data: cacheStats, isLoading, error, refetch } = useQuery({
    queryKey: ['admin-cache-stats'],
    queryFn: () => adminAPI.getCacheStats(),
    staleTime: 10 * 1000,
    refetchInterval: 30 * 1000,
  });

  useEffect(() => {
    if (error) {
      const message = error instanceof AxiosError
        ? error.response?.data?.message || 'Failed to load cache statistics'
        : 'Failed to load cache statistics';
      toast.error(message);
    }
  }, [error]);

  const clearCacheMutation = useMutation({
    mutationFn: (cacheName: string) => adminAPI.clearCache(cacheName),
    onSuccess: (data) => {
      toast.success(data.message);
      queryClient.invalidateQueries({ queryKey: ['admin-cache-stats'] });
      setSelectedCache(null);
    },
    onError: (error: AxiosError<{ message: string }>) => {
      toast.error(error.response?.data?.message || 'Failed to clear cache');
    },
  });

  const clearAllCachesMutation = useMutation({
    mutationFn: () => adminAPI.clearAllCaches(),
    onSuccess: (data) => {
      toast.success(data.message);
      queryClient.invalidateQueries({ queryKey: ['admin-cache-stats'] });
    },
    onError: (error: AxiosError<{ message: string }>) => {
      toast.error(error.response?.data?.message || 'Failed to clear all caches');
    },
  });

  const formatHitRate = (rate: number): string => {
    return `${(rate * 100).toFixed(1)}%`;
  };

  const getHitRateColor = (rate: number): string => {
    if (rate >= 0.8) return 'text-green-400';
    if (rate >= 0.5) return 'text-yellow-400';
    return 'text-red-400';
  };

  const getCacheDescription = (cacheName: string): string => {
    const descriptions: Record<string, string> = {
      products: 'Individual product details cached for faster page loads',
      productLists: 'Paginated product listings for catalog browsing',
      categories: 'Product categories (static data, long TTL)',
      dashboardStats: 'Admin dashboard aggregated statistics',
      users: 'User profile data for authenticated sessions',
    };
    return descriptions[cacheName] || 'Application cache';
  };

  const getTotalStats = (stats: CacheStatsResponse) => {
    const caches = Object.values(stats);
    return {
      totalSize: caches.reduce((sum, c) => sum + c.size, 0),
      totalHits: caches.reduce((sum, c) => sum + c.hitCount, 0),
      totalMisses: caches.reduce((sum, c) => sum + c.missCount, 0),
      totalEvictions: caches.reduce((sum, c) => sum + c.evictionCount, 0),
    };
  };

  return (
    <>
      <div className="mb-8 flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold text-white mb-2">Cache Management</h2>
          <p className="text-gray-400">Monitor and manage application caches</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={() => refetch()}
            className="btn-secondary flex items-center space-x-2 cursor-pointer"
            disabled={isLoading}
          >
            <RefreshCw size={18} className={isLoading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
          <button
            onClick={() => clearAllCachesMutation.mutate()}
            className="btn-primary bg-red-600 hover:bg-red-700 flex items-center space-x-2 cursor-pointer"
            disabled={clearAllCachesMutation.isPending}
          >
            <Trash2 size={18} />
            <span>Clear All Caches</span>
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="card p-6 animate-pulse">
              <div className="h-4 bg-gray-700 rounded w-1/2 mb-4"></div>
              <div className="h-8 bg-gray-700 rounded w-3/4"></div>
            </div>
          ))}
        </div>
      ) : cacheStats ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="card p-6 hover:border-blue-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Total Cached Items</h3>
                <Database className="text-blue-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{getTotalStats(cacheStats).totalSize}</p>
            </div>

            <div className="card p-6 hover:border-green-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Cache Hits</h3>
                <Target className="text-green-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{getTotalStats(cacheStats).totalHits.toLocaleString()}</p>
            </div>

            <div className="card p-6 hover:border-yellow-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Cache Misses</h3>
                <XCircle className="text-yellow-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{getTotalStats(cacheStats).totalMisses.toLocaleString()}</p>
            </div>

            <div className="card p-6 hover:border-purple-500 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-gray-400 text-sm font-medium">Evictions</h3>
                <Zap className="text-purple-400" size={24} />
              </div>
              <p className="text-3xl font-bold text-white">{getTotalStats(cacheStats).totalEvictions.toLocaleString()}</p>
            </div>
          </div>

          <div className="card">
            <div className="p-6 border-b border-gray-800">
              <h3 className="text-xl font-bold text-white flex items-center">
                <Activity className="mr-3 text-blue-400" size={24} />
                Cache Details
              </h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-800">
                    <th className="text-left p-4 text-gray-400 font-medium">Cache Name</th>
                    <th className="text-left p-4 text-gray-400 font-medium">Description</th>
                    <th className="text-center p-4 text-gray-400 font-medium">Size</th>
                    <th className="text-center p-4 text-gray-400 font-medium">Hits</th>
                    <th className="text-center p-4 text-gray-400 font-medium">Misses</th>
                    <th className="text-center p-4 text-gray-400 font-medium">Hit Rate</th>
                    <th className="text-center p-4 text-gray-400 font-medium">Evictions</th>
                    <th className="text-center p-4 text-gray-400 font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(cacheStats).map(([cacheName, stats]) => (
                    <tr key={cacheName} className="border-b border-gray-800 hover:bg-gray-800/50">
                      <td className="p-4">
                        <span className="text-white font-medium">{cacheName}</span>
                      </td>
                      <td className="p-4">
                        <span className="text-gray-400 text-sm">{getCacheDescription(cacheName)}</span>
                      </td>
                      <td className="p-4 text-center">
                        <span className="text-white font-mono">{stats.size}</span>
                      </td>
                      <td className="p-4 text-center">
                        <span className="text-green-400 font-mono">{stats.hitCount.toLocaleString()}</span>
                      </td>
                      <td className="p-4 text-center">
                        <span className="text-yellow-400 font-mono">{stats.missCount.toLocaleString()}</span>
                      </td>
                      <td className="p-4 text-center">
                        <span className={`font-mono font-bold ${getHitRateColor(stats.hitRate)}`}>
                          {formatHitRate(stats.hitRate)}
                        </span>
                      </td>
                      <td className="p-4 text-center">
                        <span className="text-purple-400 font-mono">{stats.evictionCount.toLocaleString()}</span>
                      </td>
                      <td className="p-4 text-center">
                        {selectedCache === cacheName ? (
                          <div className="flex items-center justify-center space-x-2">
                            <button
                              onClick={() => clearCacheMutation.mutate(cacheName)}
                              className="text-red-400 hover:text-red-300 text-sm font-medium cursor-pointer"
                              disabled={clearCacheMutation.isPending}
                            >
                              Confirm
                            </button>
                            <span className="text-gray-600">|</span>
                            <button
                              onClick={() => setSelectedCache(null)}
                              className="text-gray-400 hover:text-gray-300 text-sm cursor-pointer"
                            >
                              Cancel
                            </button>
                          </div>
                        ) : (
                          <button
                            onClick={() => setSelectedCache(cacheName)}
                            className="text-red-400 hover:text-red-300 p-2 rounded-lg hover:bg-red-900/20 transition-colors cursor-pointer"
                            title="Clear this cache"
                          >
                            <Trash2 size={18} />
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="mt-8 card p-6 bg-blue-900/10 border-blue-900">
            <h4 className="text-blue-400 font-semibold mb-3">About Caching</h4>
            <ul className="text-gray-400 text-sm space-y-2">
              <li>• <strong>Hit Rate:</strong> Percentage of requests served from cache (higher is better)</li>
              <li>• <strong>Evictions:</strong> Number of items removed due to size limits or TTL expiration</li>
              <li>• <strong>Products cache:</strong> Expires after 15 minutes or when product is updated</li>
              <li>• <strong>Dashboard stats:</strong> Expires after 5 minutes for near real-time data</li>
              <li>• <strong>Categories:</strong> Cached for 24 hours (static enum data)</li>
            </ul>
          </div>
        </>
      ) : null}
    </>
  );
};

export default AdminCache;
