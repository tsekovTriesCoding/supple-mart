import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';

import { ordersAPI } from '../lib/api/orders';
import type { OrderFilters } from '../types/order';

export const ORDERS_QUERY_KEY = 'orders';
export const ORDER_STATS_QUERY_KEY = 'order-stats';

export const useOrders = (initialFilters?: OrderFilters) => {
  const [filters, setFilters] = useState<OrderFilters>(initialFilters || {});
  const queryClient = useQueryClient();

  const {
    data: ordersData,
    isLoading: loading,
    error: queryError,
    refetch: refreshOrders,
  } = useQuery({
    queryKey: [ORDERS_QUERY_KEY, filters],
    queryFn: () => ordersAPI.getUserOrders(filters),
    staleTime: 30000,
    gcTime: 5 * 60 * 1000,
  });

  const {
    data: stats = null,
    isLoading: statsLoading,
    refetch: fetchStats,
  } = useQuery({
    queryKey: [ORDER_STATS_QUERY_KEY],
    queryFn: ordersAPI.getOrderStats,
    staleTime: 60000,
  });

  const cancelOrderMutation = useMutation({
    mutationFn: (orderId: string) => ordersAPI.cancelOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ORDERS_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: [ORDER_STATS_QUERY_KEY] });
    },
  });

  const requestReturnMutation = useMutation({
    mutationFn: ({
      orderId,
      reason,
      items,
    }: {
      orderId: string;
      reason: string;
      items?: string[];
    }) => ordersAPI.requestReturn(orderId, reason, items),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ORDERS_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: [ORDER_STATS_QUERY_KEY] });
    },
  });

  const updateFilters = (newFilters: OrderFilters) => {
    setFilters({ ...filters, ...newFilters });
  };

  const getOrderById = async (orderId: string) => {
    return await ordersAPI.getOrderById(orderId);
  };

  const trackOrder = async (orderNumber: string) => {
    return await ordersAPI.trackOrder(orderNumber);
  };

  const error = queryError ? 'Failed to load orders' : null;

  return {
    orders: ordersData?.orders || [],
    stats,
    loading,
    statsLoading,
    error,
    totalElements: ordersData?.totalElements || 0,
    totalPages: ordersData?.totalPages || 0,
    currentPage: ordersData?.currentPage || 1,
    filters,
    getOrderById,
    cancelOrder: cancelOrderMutation.mutateAsync,
    trackOrder,
    requestReturn: (orderId: string, reason: string, items?: string[]) =>
      requestReturnMutation.mutateAsync({ orderId, reason, items }),
    updateFilters,
    refreshOrders,
    fetchStats,
  };
};
