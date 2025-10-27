export interface Product {
  id: number;
  name: string;
  price: number;
  originalPrice?: number;
  averageRating: number;
  totalReviews: number;
  imageUrl: string;
  category: string;
  brand?: string;
  inStock: boolean;
  description: string;
  tags?: string[];
  stock?: number;
  createdAt?: string;
  updatedAt?: string;
  reviews?: Review[];
}

export interface Review {
  id: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductQueryParams {
  page?: number;
  limit?: number;
  category?: string;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: 'name' | 'price' | 'createdAt';
  sortOrder?: 'asc' | 'desc';
}

export interface ProductSearchFilters {
  category?: string;
  minPrice?: number;
  maxPrice?: number;
}

export interface ProductData {
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl?: string;
  stock?: number;
}

export interface ProductUpdateData {
  name?: string;
  description?: string;
  price?: number;
  category?: string;
  imageUrl?: string;
  stock?: number;
}

export type StockStatus = 'in-stock' | 'low-stock' | 'out-of-stock';
