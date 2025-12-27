import type { Page } from '@playwright/test';

import {
  mockProducts,
  mockCategories,
  mockCartItems,
  mockReviews,
  mockWishlistItems,
  mockOrders,
  mockOrderStats,
  mockUserReviews,
  mockNotificationPreferences,
  mockPrivacySettings,
  mockUser,
} from './data';

/**
 * Setup API mocks for E2E tests
 * This allows tests to run without a backend server
 */
export async function setupAPIMocks(page: Page) {
  // Helper to handle product list requests
  const handleProductListRequest = async (route: import('@playwright/test').Route) => {
    const url = new URL(route.request().url());
    const search = url.searchParams.get('search')?.toLowerCase() || '';
    const category = url.searchParams.get('category');
    // Frontend sends 0-indexed page (page=0 for first page)
    const page_num = parseInt(url.searchParams.get('page') || '0');
    const limit = parseInt(url.searchParams.get('limit') || '12');

    let filtered = [...mockProducts];

    if (search) {
      filtered = filtered.filter(
        (p) =>
          p.name.toLowerCase().includes(search) ||
          p.description.toLowerCase().includes(search)
      );
    }

    if (category) {
      filtered = filtered.filter((p) => p.category === category);
    }

    const start = page_num * limit;
    const paged = filtered.slice(start, start + limit);

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        products: paged,
        pagination: {
          page: page_num,
          limit,
          total: filtered.length,
          totalPages: Math.ceil(filtered.length / limit),
        },
      }),
    });
  };

  // Mock products list with query params
  await page.route('**/api/products?*', handleProductListRequest);
  
  // Mock products list without query params (initial load)
  await page.route(/\/api\/products$/, handleProductListRequest);

  // Mock categories - must be before single product route
  await page.route('**/api/products/categories', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockCategories),
    });
  });

  // Mock single product (must be after categories)
  await page.route(/\/api\/products\/(?!categories)[^?]+/, async (route) => {
    const url = route.request().url();
    const match = url.match(/\/api\/products\/([^?]+)/);
    const id = match ? match[1] : '';
    
    const product = mockProducts.find((p) => p.id === id);

    if (product) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(product),
      });
    } else {
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Product not found' }),
      });
    }
  });

  // Mock cart - returns Cart object format
  await page.route('**/api/cart', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'cart-1',
          userId: 'user-1',
          items: mockCartItems,
          createdAt: '2024-03-01T10:00:00Z',
          updatedAt: '2024-03-10T10:00:00Z',
        }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    }
  });

  // Mock cart item operations
  await page.route('**/api/cart/**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true }),
    });
  });

  // Mock wishlist - returns WishlistResponse format
  await page.route('**/api/wishlist', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: mockWishlistItems,
          currentPage: 0,
          pageSize: 10,
          totalPages: 1,
          totalElements: mockWishlistItems.length,
        }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    }
  });

  // Mock wishlist operations
  await page.route('**/api/wishlist/**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true }),
    });
  });

  // Mock orders - handle with or without query params
  // Use glob pattern that matches any URL containing /api/orders with optional query params
  await page.route('**/api/orders?*', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          orders: mockOrders,
          totalElements: mockOrders.length,
          totalPages: 1,
          currentPage: 1,
        }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, orderId: 'new-order-1' }),
      });
    }
  });
  
  // Also match orders without query params
  await page.route('**/api/orders', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          orders: mockOrders,
          totalElements: mockOrders.length,
          totalPages: 1,
          currentPage: 1,
        }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, orderId: 'new-order-1' }),
      });
    }
  });

  // Mock order stats - MUST be before wildcard /api/orders/* route
  // Use exact match to avoid being overridden
  await page.route(/\/api\/orders\/stats$/, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockOrderStats),
    });
  });

  // Mock single order - MUST be after /api/orders/stats
  // Use regex that excludes 'stats'
  await page.route(/\/api\/orders\/(?!stats)[^?/]+$/, async (route) => {
    const url = route.request().url();
    const id = url.split('/').pop();
    const order = mockOrders.find((o) => o.id === id);

    if (order) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(order),
      });
    } else {
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Order not found' }),
      });
    }
  });

  // Mock reviews - returns user's reviews as array
  await page.route('**/api/reviews', async (route) => {
    if (route.request().method() === 'GET') {
      // getUserReviews returns array directly
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockUserReviews),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    }
  });

  // Mock product reviews
  await page.route('**/api/reviews/product/*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockReviews),
    });
  });

  // Mock contact form
  await page.route('**/api/contact', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, message: 'Message sent successfully' }),
    });
  });

  // Mock auth check (unauthenticated by default)
  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Not authenticated' }),
    });
  });

  // Mock OAuth2 providers
  await page.route('**/api/auth/oauth2/providers', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        providers: [
          { name: 'google', enabled: true },
          { name: 'github', enabled: true },
        ],
      }),
    });
  });

  // Mock login
  await page.route('**/api/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        user: mockUser,
      }),
    });
  });

  // Mock register
  await page.route('**/api/auth/register', async (route) => {
    await route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        user: mockUser,
      }),
    });
  });

  // Mock notification preferences - endpoint is /notification-preferences
  await page.route('**/api/notification-preferences', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockNotificationPreferences),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockNotificationPreferences),
      });
    }
  });

  // Mock privacy settings - endpoint is /privacy-settings
  await page.route('**/api/privacy-settings', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockPrivacySettings),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockPrivacySettings),
      });
    }
  });

  // Mock user profile
  await page.route('**/api/user/profile', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockUser),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    }
  });

  // Mock payments
  await page.route('**/api/payments/**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ 
        clientSecret: 'mock_client_secret',
        success: true 
      }),
    });
  });
}

/**
 * Setup mocks for authenticated user
 */
export async function setupAuthenticatedMocks(page: Page) {
  await setupAPIMocks(page);

  // Override auth check to return authenticated user
  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockUser),
    });
  });
}

/**
 * Setup mocks with empty cart
 */
export async function setupEmptyCartMocks(page: Page) {
  await setupAPIMocks(page);

  // Override cart to be empty - use Cart format
  await page.route('**/api/cart', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 'cart-1',
        userId: 'user-1',
        items: [],
        createdAt: '2024-03-01T10:00:00Z',
        updatedAt: '2024-03-10T10:00:00Z',
      }),
    });
  });
}

/**
 * Setup mocks with empty wishlist
 */
export async function setupEmptyWishlistMocks(page: Page) {
  await setupAuthenticatedMocks(page);

  // Override wishlist to be empty - use WishlistResponse format
  await page.route('**/api/wishlist', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: [],
        currentPage: 0,
        pageSize: 10,
        totalPages: 0,
        totalElements: 0,
      }),
    });
  });
}
