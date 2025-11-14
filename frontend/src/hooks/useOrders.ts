import { useState, useEffect } from 'react';

import { ordersAPI } from '../lib/api/orders';
import type { Order, OrderFilters, OrdersResponse, OrderStats } from '../types/order';

export const useOrders = (initialFilters?: OrderFilters) => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [stats, setStats] = useState<OrderStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [filters, setFilters] = useState<OrderFilters>(initialFilters || {});

  const fetchOrders = async (newFilters?: OrderFilters) => {
    try {
      setLoading(true);
      setError(null);
      
      const filtersToUse = newFilters || filters;
      const response: OrdersResponse = await ordersAPI.getUserOrders(filtersToUse);
      
      setOrders(response.orders);
      setTotalElements(response.totalElements);
      setTotalPages(response.totalPages);
      setCurrentPage(response.currentPage);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  const getOrderById = async (orderId: string) => {
    try {
      setError(null);
      const order = await ordersAPI.getOrderById(orderId);
      return order;
    } catch (err) {
      console.error('Error fetching order:', err);
      setError('Failed to load order details');
      throw err;
    }
  };

  const cancelOrder = async (orderId: string) => {
    try {
      setError(null);
      const updatedOrder = await ordersAPI.cancelOrder(orderId);
      
      setOrders(prev => 
        prev.map(order => 
          order.id === orderId ? updatedOrder : order
        )
      );
      
      return updatedOrder;
    } catch (err) {
      console.error('Error cancelling order:', err);
      setError('Failed to cancel order');
      throw err;
    }
  };

  const trackOrder = async (orderNumber: string) => {
    try {
      setError(null);
      const order = await ordersAPI.trackOrder(orderNumber);
      return order;
    } catch (err) {
      console.error('Error tracking order:', err);
      setError('Failed to track order');
      throw err;
    }
  };

  const requestReturn = async (orderId: string, reason: string, items?: string[]) => {
    try {
      setError(null);
      const result = await ordersAPI.requestReturn(orderId, reason, items);
      
      await fetchOrders();
      
      return result;
    } catch (err) {
      console.error('Error requesting return:', err);
      setError('Failed to request return');
      throw err;
    }
  };

  const updateFilters = (newFilters: OrderFilters) => {
    const updatedFilters = { ...filters, ...newFilters };
    setFilters(updatedFilters);
    fetchOrders(updatedFilters);
  };

  const refreshOrders = () => {
    fetchOrders();
  };

  const fetchStats = async () => {
    try {
      setStatsLoading(true);
      const statsData = await ordersAPI.getOrderStats();
      setStats(statsData);
    } catch (err) {
      console.error('Error fetching order stats:', err);
    } finally {
      setStatsLoading(false);
    }
  };

  useEffect(() => {
    const loadOrders = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response: OrdersResponse = await ordersAPI.getUserOrders(filters);
        
        setOrders(response.orders);
        setTotalElements(response.totalElements);
        setTotalPages(response.totalPages);
        setCurrentPage(response.currentPage);
      } catch (err) {
        console.error('Error fetching orders:', err);
        setError('Failed to load orders');
      } finally {
        setLoading(false);
      }
    };

    loadOrders();
  }, [filters]);

  useEffect(() => {
    fetchStats();
  }, []);

  return {
    orders,
    stats,
    loading,
    statsLoading,
    error,
    totalElements,
    totalPages,
    currentPage,
    filters,
    getOrderById,
    cancelOrder,
    trackOrder,
    requestReturn,
    updateFilters,
    refreshOrders,
    fetchStats
  };
};
