import { useReducer, useEffect, useCallback } from 'react';

import { ordersAPI } from '../lib/api/orders';
import type { OrderFilters, OrdersResponse } from '../types/order';
import { ordersReducer, createInitialOrdersState } from '../reducers/ordersReducer';

export const useOrders = (initialFilters?: OrderFilters) => {
  const [state, dispatch] = useReducer(ordersReducer, createInitialOrdersState(initialFilters));

  const fetchOrders = useCallback(async (newFilters?: OrderFilters) => {
    try {
      dispatch({ type: 'FETCH_START' });
      
      const filtersToUse = newFilters || state.filters;
      const response: OrdersResponse = await ordersAPI.getUserOrders(filtersToUse);
      
      dispatch({
        type: 'FETCH_SUCCESS',
        payload: {
          orders: response.orders,
          totalElements: response.totalElements,
          totalPages: response.totalPages,
          currentPage: response.currentPage,
        },
      });
    } catch (err) {
      console.error('Error fetching orders:', err);
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to load orders' });
    }
  }, [state.filters]);

  const getOrderById = useCallback(async (orderId: string) => {
    try {
      dispatch({ type: 'CLEAR_ERROR' });
      const order = await ordersAPI.getOrderById(orderId);
      return order;
    } catch (err) {
      console.error('Error fetching order:', err);
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to load order details' });
      throw err;
    }
  }, []);

  const cancelOrder = useCallback(async (orderId: string) => {
    try {
      dispatch({ type: 'CLEAR_ERROR' });
      const updatedOrder = await ordersAPI.cancelOrder(orderId);
      
      dispatch({ type: 'UPDATE_ORDER', payload: updatedOrder });
      
      return updatedOrder;
    } catch (err) {
      console.error('Error cancelling order:', err);
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to cancel order' });
      throw err;
    }
  }, []);

  const trackOrder = useCallback(async (orderNumber: string) => {
    try {
      dispatch({ type: 'CLEAR_ERROR' });
      const order = await ordersAPI.trackOrder(orderNumber);
      return order;
    } catch (err) {
      console.error('Error tracking order:', err);
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to track order' });
      throw err;
    }
  }, []);

  const requestReturn = useCallback(async (orderId: string, reason: string, items?: string[]) => {
    try {
      dispatch({ type: 'CLEAR_ERROR' });
      const result = await ordersAPI.requestReturn(orderId, reason, items);
      
      await fetchOrders();
      
      return result;
    } catch (err) {
      console.error('Error requesting return:', err);
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to request return' });
      throw err;
    }
  }, [fetchOrders]);

  const updateFilters = useCallback((newFilters: OrderFilters) => {
    dispatch({ type: 'SET_FILTERS', payload: newFilters });
    fetchOrders(newFilters);
  }, [fetchOrders]);

  const refreshOrders = useCallback(() => {
    fetchOrders();
  }, [fetchOrders]);

  const fetchStats = useCallback(async () => {
    try {
      dispatch({ type: 'STATS_FETCH_START' });
      const statsData = await ordersAPI.getOrderStats();
      dispatch({ type: 'STATS_FETCH_SUCCESS', payload: statsData });
    } catch (err) {
      console.error('Error fetching order stats:', err);
      dispatch({ type: 'STATS_FETCH_ERROR' });
    }
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [state.filters, fetchOrders]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return {
    orders: state.orders,
    stats: state.stats,
    loading: state.loading,
    statsLoading: state.statsLoading,
    error: state.error,
    totalElements: state.totalElements,
    totalPages: state.totalPages,
    currentPage: state.currentPage,
    filters: state.filters,
    getOrderById,
    cancelOrder,
    trackOrder,
    requestReturn,
    updateFilters,
    refreshOrders,
    fetchStats
  };
};
