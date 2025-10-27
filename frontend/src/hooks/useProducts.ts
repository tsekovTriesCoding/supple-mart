import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { productsAPI } from '../lib/api';
import type { Product, StockStatus } from '../types/product';

export type { 
  Product, 
  Review, 
  ProductQueryParams, 
  ProductSearchFilters,
  ProductData,
  ProductUpdateData,
  StockStatus 
} from '../types/product';

export const useProducts = (params?: {
  page?: number;
  limit?: number;
  category?: string;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: 'name' | 'price' | 'createdAt';
  sortOrder?: 'asc' | 'desc';
}) => {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => productsAPI.getProducts(params),
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

export const useProduct = (id: string | number) => {
  return useQuery({
    queryKey: ['product', id],
    queryFn: () => productsAPI.getProductById(id),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
  });
};

export const useProductCategories = () => {
  return useQuery({
    queryKey: ['productCategories'],
    queryFn: () => productsAPI.getCategories(),
    staleTime: 30 * 60 * 1000,
  });
};

export const useProductSearch = (searchTerm: string, filters?: {
  category?: string;
  minPrice?: number;
  maxPrice?: number;
}) => {
  return useQuery({
    queryKey: ['productSearch', searchTerm, filters],
    queryFn: () => productsAPI.searchProducts(searchTerm, filters),
    enabled: !!searchTerm && searchTerm.length > 2,
    staleTime: 2 * 60 * 1000,
  });
};

export const useFeaturedProducts = (limit?: number) => {
  return useQuery({
    queryKey: ['featuredProducts', limit],
    queryFn: () => productsAPI.getFeaturedProducts(limit),
    staleTime: 10 * 60 * 1000,
  });
};

export const useProductsByCategory = (category: string, params?: {
  page?: number;
  limit?: number;
  sortBy?: 'name' | 'price' | 'createdAt';
  sortOrder?: 'asc' | 'desc';
}) => {
  return useQuery({
    queryKey: ['productsByCategory', category, params],
    queryFn: () => productsAPI.getProductsByCategory(category, params),
    enabled: !!category,
    staleTime: 5 * 60 * 1000,
  });
};

export const useCreateProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: productsAPI.createProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      queryClient.invalidateQueries({ queryKey: ['featuredProducts'] });
    },
  });
};

export const useUpdateProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: {
      id: string | number;
      data: {
        name?: string;
        description?: string;
        price?: number;
        category?: string;
        imageUrl?: string;
        stock?: number;
      }
    }) => productsAPI.updateProduct(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['product', id] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });
};

export const useDeleteProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: productsAPI.deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      queryClient.invalidateQueries({ queryKey: ['featuredProducts'] });
    },
  });
};

export const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
};

export const calculateDiscountPercentage = (originalPrice: number, currentPrice: number): number => {
  return Math.round(((originalPrice - currentPrice) / originalPrice) * 100);
};

export const isProductOnSale = (product: Product): boolean => {
  return !!(product.originalPrice && product.originalPrice > product.price);
};

export const getStockStatus = (stock: number): StockStatus => {
  if (stock === 0) return 'out-of-stock';
  if (stock < 10) return 'low-stock';
  return 'in-stock';
};
