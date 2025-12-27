/**
 * Mock data for E2E tests
 * These mocks allow tests to run without a backend
 */

export const mockProducts = [
  {
    id: '1',
    name: 'Premium Whey Protein',
    description: 'High-quality whey protein for muscle building',
    price: 49.99,
    category: 'protein',
    brand: 'FitPro',
    imageUrl: 'https://picsum.photos/seed/protein1/400/400',
    inStock: true,
    stock: 50,
    averageRating: 4.5,
    totalReviews: 128,
    createdAt: '2024-01-15T10:00:00Z',
  },
  {
    id: '2',
    name: 'BCAA Energy Drink',
    description: 'Branch chain amino acids for recovery',
    price: 29.99,
    category: 'amino-acids',
    brand: 'NutriMax',
    imageUrl: 'https://picsum.photos/seed/bcaa1/400/400',
    inStock: true,
    stock: 100,
    averageRating: 4.2,
    totalReviews: 85,
    createdAt: '2024-02-10T10:00:00Z',
  },
  {
    id: '3',
    name: 'Creatine Monohydrate',
    description: 'Pure creatine for strength and power',
    price: 24.99,
    category: 'creatine',
    brand: 'PowerLift',
    imageUrl: 'https://picsum.photos/seed/creatine1/400/400',
    inStock: false,
    stock: 0,
    averageRating: 4.8,
    totalReviews: 256,
    createdAt: '2024-01-20T10:00:00Z',
  },
  {
    id: '4',
    name: 'Pre-Workout Extreme',
    description: 'Explosive energy for intense workouts',
    price: 39.99,
    category: 'pre-workout',
    brand: 'FitPro',
    imageUrl: 'https://picsum.photos/seed/preworkout1/400/400',
    inStock: true,
    stock: 75,
    averageRating: 4.3,
    totalReviews: 192,
    createdAt: '2024-03-05T10:00:00Z',
  },
];

export const mockCategories = [
  'protein',
  'amino-acids',
  'creatine',
  'pre-workout',
  'vitamins',
];

export const mockUser = {
  id: 'user-1',
  email: 'test@example.com',
  firstName: 'John',
  lastName: 'Doe',
  role: 'USER',
};

export const mockAdminUser = {
  id: 'admin-1',
  email: 'admin@example.com',
  firstName: 'Admin',
  lastName: 'User',
  role: 'ADMIN',
};

export const mockAuthResponse = {
  accessToken: 'mock-access-token',
  refreshToken: 'mock-refresh-token',
  user: mockUser,
};

export const mockCartItems = [
  {
    id: 'cart-item-1',
    productId: '1',
    productName: 'Premium Whey Protein',
    price: 49.99,
    quantity: 2,
    productImageUrl: 'https://picsum.photos/seed/protein1/400/400',
    subtotal: 99.98,
  },
  {
    id: 'cart-item-2',
    productId: '2',
    productName: 'BCAA Energy Drink',
    price: 29.99,
    quantity: 1,
    productImageUrl: 'https://picsum.photos/seed/bcaa1/400/400',
    subtotal: 29.99,
  },
];

export const mockWishlistItems = [
  {
    id: 'wishlist-1',
    productId: '1',
    productName: 'Premium Whey Protein',
    productDescription: 'High-quality whey protein for muscle building',
    price: 49.99,
    category: 'protein',
    imageUrl: 'https://picsum.photos/seed/protein1/400/400',
    inStock: true,
    stockQuantity: 50,
    averageRating: 4.5,
    totalReviews: 128,
    addedAt: '2024-03-01T10:00:00Z',
  },
  {
    id: 'wishlist-2',
    productId: '4',
    productName: 'Pre-Workout Extreme',
    productDescription: 'Explosive energy for intense workouts',
    price: 39.99,
    category: 'pre-workout',
    imageUrl: 'https://picsum.photos/seed/preworkout1/400/400',
    inStock: true,
    stockQuantity: 75,
    averageRating: 4.3,
    totalReviews: 192,
    addedAt: '2024-03-02T10:00:00Z',
  },
];

export const mockOrders = [
  {
    id: 'order-1',
    orderNumber: 'ORD-2024-001',
    status: 'DELIVERED',
    totalAmount: 129.97,
    createdAt: '2024-03-01T10:00:00Z',
    items: [
      {
        id: 'item-1',
        productId: '1',
        productName: 'Premium Whey Protein',
        quantity: 2,
        price: 49.99,
        product: {
          id: '1',
          name: 'Premium Whey Protein',
          imageUrl: 'https://picsum.photos/seed/protein1/400/400',
        },
      },
      {
        id: 'item-2',
        productId: '2',
        productName: 'BCAA Energy Drink',
        quantity: 1,
        price: 29.99,
        product: {
          id: '2',
          name: 'BCAA Energy Drink',
          imageUrl: 'https://picsum.photos/seed/bcaa1/400/400',
        },
      },
    ],
  },
  {
    id: 'order-2',
    orderNumber: 'ORD-2024-002',
    status: 'PROCESSING',
    totalAmount: 39.99,
    createdAt: '2024-03-10T10:00:00Z',
    items: [
      {
        id: 'item-3',
        productId: '4',
        productName: 'Pre-Workout Extreme',
        quantity: 1,
        price: 39.99,
        product: {
          id: '4',
          name: 'Pre-Workout Extreme',
          imageUrl: 'https://picsum.photos/seed/preworkout1/400/400',
        },
      },
    ],
  },
  {
    id: 'order-3',
    orderNumber: 'ORD-2024-003',
    status: 'PENDING',
    totalAmount: 24.99,
    createdAt: '2024-03-15T10:00:00Z',
    items: [
      {
        id: 'item-4',
        productId: '3',
        productName: 'Creatine Monohydrate',
        quantity: 1,
        price: 24.99,
        product: {
          id: '3',
          name: 'Creatine Monohydrate',
          imageUrl: 'https://picsum.photos/seed/creatine1/400/400',
        },
      },
    ],
  },
];

export const mockOrderStats = {
  totalOrders: 3,
  pendingCount: 1,
  paidCount: 0,
  processingCount: 1,
  shippedCount: 0,
  deliveredCount: 1,
  cancelledCount: 0,
  totalSpent: 194.95,
};

export const mockReviews = [
  {
    id: 'review-1',
    productId: '1',
    userId: 'user-1',
    userName: 'John Doe',
    rating: 5,
    comment: 'Excellent product! Great taste and mixes well.',
    createdAt: '2024-03-01T10:00:00Z',
  },
  {
    id: 'review-2',
    productId: '1',
    userId: 'user-2',
    userName: 'Jane Smith',
    rating: 4,
    comment: 'Good quality protein, but a bit pricey.',
    createdAt: '2024-02-28T10:00:00Z',
  },
];

export const mockUserReviews = [
  {
    id: 'review-1',
    rating: 5,
    comment: 'Excellent product! Great taste and mixes well.',
    createdAt: '2024-03-01T10:00:00Z',
    updatedAt: '2024-03-01T10:00:00Z',
    user: {
      id: 'user-1',
      name: 'John Doe',
      email: 'test@example.com',
    },
    product: {
      id: '1',
      name: 'Premium Whey Protein',
      imageUrl: 'https://picsum.photos/seed/protein1/400/400',
      price: 49.99,
    },
  },
  {
    id: 'review-2',
    rating: 4,
    comment: 'Helps with recovery after workouts. Tastes decent.',
    createdAt: '2024-02-15T10:00:00Z',
    updatedAt: '2024-02-15T10:00:00Z',
    user: {
      id: 'user-1',
      name: 'John Doe',
      email: 'test@example.com',
    },
    product: {
      id: '2',
      name: 'BCAA Energy Drink',
      imageUrl: 'https://picsum.photos/seed/bcaa1/400/400',
      price: 29.99,
    },
  },
];

export const mockNotificationPreferences = {
  emailNotifications: true,
  orderUpdates: true,
  promotions: false,
  newsletter: true,
};

export const mockPrivacySettings = {
  profileVisibility: 'private',
  showOrderHistory: false,
  allowDataCollection: true,
};
