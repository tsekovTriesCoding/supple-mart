export interface WishlistItem {
  id: string;
  productId: string;
  productName: string;
  productDescription: string;
  price: number;
  originalPrice?: number;
  category: string;
  imageUrl?: string;
  inStock: boolean;
  stockQuantity: number;
  averageRating: number;
  totalReviews: number;
  addedAt: string;
}

export interface WishlistResponse {
  content: WishlistItem[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

export interface AddToWishlistRequest {
  productId: string;
}
