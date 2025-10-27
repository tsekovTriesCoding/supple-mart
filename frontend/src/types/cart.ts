export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productImageUrl: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface AddCartItemRequest {
  productId: string;
  quantity: number;
}

export const formatCartPrice = (price: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(price);
};
