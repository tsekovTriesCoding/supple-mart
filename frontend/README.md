# SuppleMart Frontend

The SuppleMart frontend is a modern React application built with TypeScript and Vite. It provides an intuitive user interface for browsing products, managing shopping carts, processing payments, and accessing administrative features.

## Table of Contents

- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Components](#components)
- [Custom Hooks](#custom-hooks)
- [API Integration](#api-integration)
- [Routing](#routing)
- [State Management](#state-management)
- [Styling](#styling)
- [Testing](#testing)
- [Building](#building)

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.1.1 | UI framework |
| TypeScript | 5.x | Type safety |
| Vite | 6.x | Build tool and dev server |
| TanStack Query | 5.90.5 | Server state management |
| React Router | 7.9.4 | Client-side routing |
| React Hook Form | 7.65.0 | Form handling |
| Tailwind CSS | 4.x | Utility-first styling |
| Axios | 1.12.2 | HTTP client |
| Stripe React | 5.3.0 | Payment integration |
| Lucide React | 0.546.0 | Icon library |
| Vitest | 3.x | Unit testing |
| Playwright | 1.52.0 | E2E testing |
| React Testing Library | Latest | Component testing |

## Project Structure

```
src/
|-- components/                 # Reusable UI components
|   |-- Form/                   # Form components
|   |   |-- FormInput.tsx
|   |   |-- FormSelect.tsx
|   |   +-- FormTextarea.tsx
|   |-- Header/                 # Header components
|   |   |-- Header.tsx
|   |   |-- MobileNav.tsx
|   |   |-- SearchInput.tsx
|   |   +-- UserDropdown.tsx
|   |-- Product/                # Product components
|   |   |-- ProductCard.tsx
|   |   |-- ProductFilters.tsx
|   |   +-- ProductReviews.tsx
|   |-- AuthModal.tsx           # Authentication modal
|   |-- CartProvider.tsx        # Cart context provider
|   |-- LoadingSpinner.tsx      # Loading indicator
|   |-- Pagination.tsx          # Pagination controls
|   |-- PasswordChangeModal.tsx # Password change form
|   |-- PaymentForm.tsx         # Stripe payment form
|   |-- ProtectedRoute.tsx      # Auth route guard
|   |-- ReviewModal.tsx         # Review submission modal
|   +-- StarRating.tsx          # Star rating display
|
|-- hooks/                      # Custom React hooks
|   |-- useAuth.ts              # Authentication logic
|   |-- useCart.ts              # Cart operations
|   |-- useProducts.ts          # Product fetching
|   +-- useWishlist.ts          # Wishlist operations
|
|-- lib/                        # Libraries and utilities
|   |-- api.ts                  # Axios instance configuration
|   |-- stripe.ts               # Stripe configuration
|   +-- api/                    # API modules by domain
|       |-- admin.ts            # Admin API calls
|       |-- auth.ts             # Authentication API
|       |-- cart.ts             # Cart API
|       |-- contact.ts          # Contact form API
|       |-- notification.ts     # Notification preferences API
|       |-- orders.ts           # Orders API
|       |-- payments.ts         # Payments API
|       |-- privacy.ts          # Privacy settings API
|       |-- products.ts         # Products API
|       |-- reviews.ts          # Reviews API
|       |-- user.ts             # User profile API
|       +-- wishlist.ts         # Wishlist API
|
|-- pages/                      # Page components
|   |-- About.tsx               # About page
|   |-- Account.tsx             # User account
|   |-- Cart.tsx                # Shopping cart
|   |-- Checkout.tsx            # Checkout process
|   |-- Contact.tsx             # Contact form
|   |-- Home.tsx                # Landing page
|   |-- NotificationPreferences.tsx
|   |-- OAuth2Callback.tsx      # OAuth redirect handler
|   |-- Orders.tsx              # Order history
|   |-- PrivacySettings.tsx     # Privacy options
|   |-- ProductDetail.tsx       # Single product view
|   |-- Products.tsx            # Product listing
|   |-- Reviews.tsx             # User reviews
|   |-- Wishlist.tsx            # User wishlist
|   +-- admin/                  # Admin pages
|       |-- AdminCache.tsx      # Cache management
|       |-- AdminDashboard.tsx  # Admin dashboard
|       |-- AdminLayout.tsx     # Admin layout wrapper
|       |-- AdminOrders.tsx     # Order management
|       |-- AdminProducts.tsx   # Product management
|       +-- AdminUsers.tsx      # User management
|
|-- reducers/                   # State reducers
|   +-- cartReducer.ts          # Cart state management
|
|-- styles/                     # CSS styles
|   +-- custom.css              # Custom styles
|
|-- test/                       # Test utilities
|   +-- setup.ts                # Test configuration
|
|-- types/                      # TypeScript definitions
|   +-- index.ts                # Type exports
|
|-- utils/                      # Utility functions
|   +-- categoryUtils.ts        # Category helpers
|
|-- App.tsx                     # Root component with routing
|-- main.tsx                    # Application entry point
+-- index.css                   # Global styles

tests/
+-- e2e/                        # End-to-end tests (Playwright)
```

## Getting Started

### Prerequisites

- Node.js 20 or higher
- npm or yarn

### Installation

1. Install dependencies:
   ```bash
   npm install
   ```

2. Create a `.env` file in the frontend directory:
   ```env
   VITE_API_URL=http://localhost:8080/api/
   VITE_BACKEND_URL=http://localhost:8080
   VITE_STRIPE_PUBLISHABLE_KEY=pk_test_your_key
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

4. Open http://localhost:5173 in your browser.

## Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `VITE_API_URL` | Backend API base URL (with trailing slash) | Yes |
| `VITE_BACKEND_URL` | Backend base URL for OAuth redirects | Yes |
| `VITE_STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | Yes |

### Build Configuration

- **Vite**: `vite.config.ts` - Build and dev server configuration
- **TypeScript**: `tsconfig.json`, `tsconfig.app.json`, `tsconfig.node.json`
- **Tailwind**: `tailwind.config.js`, `postcss.config.js`
- **ESLint**: `eslint.config.js`

## Components

### Core Components

| Component | Description |
|-----------|-------------|
| `AuthModal` | Modal with login/register forms and OAuth2 buttons |
| `CartProvider` | Context provider for cart state management |
| `LoadingSpinner` | Animated loading indicator |
| `Pagination` | Pagination controls with page numbers |
| `PasswordChangeModal` | Form for changing user password |
| `PaymentForm` | Stripe Elements payment form |
| `ProtectedRoute` | Route wrapper requiring authentication |
| `ReviewModal` | Modal for creating/editing reviews |
| `StarRating` | Interactive star rating component |

### Form Components

| Component | Description |
|-----------|-------------|
| `FormInput` | Text input with label and error handling |
| `FormSelect` | Select dropdown with label and error handling |
| `FormTextarea` | Textarea with label and error handling |

### Header Components

| Component | Description |
|-----------|-------------|
| `Header` | Main navigation header |
| `MobileNav` | Mobile navigation menu |
| `SearchInput` | Product search input |
| `UserDropdown` | User menu dropdown |

### Product Components

| Component | Description |
|-----------|-------------|
| `ProductCard` | Product display card with image and actions |
| `ProductFilters` | Category and price filters |
| `ProductReviews` | Product reviews display |

## Custom Hooks

| Hook | Purpose |
|------|---------|
| `useAuth` | Authentication state and actions (login, logout, register) |
| `useCart` | Cart operations (add, remove, update, clear) |
| `useProducts` | Product fetching with pagination and filters |
| `useWishlist` | Wishlist operations (add, remove, check) |

### Usage Examples

```typescript
// Authentication
const { user, isAuthenticated, login, logout } = useAuth();

// Cart operations
const { cart, addItem, removeItem, updateQuantity } = useCart();

// Products with filters
const { products, isLoading, error } = useProducts({
  page: 1,
  category: 'electronics',
  sortBy: 'price'
});

// Wishlist
const { wishlist, addToWishlist, removeFromWishlist } = useWishlist();
```

## API Integration

### API Client

The API client is configured in `lib/api.ts` with:
- Base URL from environment variables
- Request interceptor for JWT authentication
- Response interceptor for token refresh on 401 errors

### API Modules

Each domain has a dedicated API module in `lib/api/`:

| Module | Endpoints |
|--------|-----------|
| `auth.ts` | Login, register, logout, refresh token |
| `products.ts` | List, search, get product details |
| `cart.ts` | Get cart, add/update/remove items |
| `orders.ts` | Create order, list orders, get order details |
| `payments.ts` | Create payment intent |
| `reviews.ts` | CRUD operations for reviews |
| `wishlist.ts` | Add/remove wishlist items |
| `user.ts` | Profile operations, password change |
| `admin.ts` | Admin dashboard, product/order/user management |

## Routing

### Public Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/` | `Home` | Landing page |
| `/products` | `Products` | Product listing with filters |
| `/products/:id` | `ProductDetail` | Product details and reviews |
| `/about` | `About` | About page |
| `/contact` | `Contact` | Contact form |
| `/oauth2/callback` | `OAuth2Callback` | OAuth2 redirect handler |

### Protected Routes

Require authentication:

| Path | Component | Description |
|------|-----------|-------------|
| `/cart` | `Cart` | Shopping cart |
| `/checkout` | `Checkout` | Payment and order creation |
| `/orders` | `Orders` | Order history |
| `/wishlist` | `Wishlist` | Saved products |
| `/account` | `Account` | User profile |
| `/reviews` | `Reviews` | User's reviews |
| `/notifications` | `NotificationPreferences` | Email settings |
| `/privacy` | `PrivacySettings` | Privacy options |

### Admin Routes

Require admin role:

| Path | Component | Description |
|------|-----------|-------------|
| `/admin` | `AdminDashboard` | Dashboard with statistics |
| `/admin/products` | `AdminProducts` | Product management |
| `/admin/orders` | `AdminOrders` | Order management |
| `/admin/users` | `AdminUsers` | User management |
| `/admin/cache` | `AdminCache` | Cache management |

## State Management

### Server State (TanStack Query)

Server state is managed using TanStack Query for:
- Automatic caching and cache invalidation
- Background refetching
- Loading and error states
- Optimistic updates

### Client State (Context + useReducer)

Cart state uses React Context with useReducer:

```typescript
// Cart actions
type CartAction =
  | { type: 'SET_CART'; payload: Cart }
  | { type: 'ADD_ITEM'; payload: CartItem }
  | { type: 'REMOVE_ITEM'; payload: number }
  | { type: 'UPDATE_QUANTITY'; payload: { id: number; quantity: number } }
  | { type: 'CLEAR_CART' };
```

### Authentication State

Authentication state is stored in localStorage:
- `accessToken` - JWT access token
- `refreshToken` - JWT refresh token
- `user` - User profile data

## Styling

### Tailwind CSS

The application uses Tailwind CSS for styling:
- Utility-first approach
- Responsive design with breakpoints (`sm`, `md`, `lg`, `xl`)
- Custom color palette defined in `tailwind.config.js`

### Custom Styles

Additional styles in `src/styles/custom.css` for:
- Animation keyframes
- Component-specific styles
- Third-party library overrides

## Testing

### Unit Tests (Vitest)

Run unit tests:
```bash
npm run test
```

Run with coverage:
```bash
npm run test:coverage
```

Watch mode:
```bash
npm run test:watch
```

### E2E Tests (Playwright)

Run E2E tests:
```bash
npm run test:e2e
```

Run with UI:
```bash
npm run test:e2e:ui
```

### Test Files

- Unit tests are co-located with components (e.g., `Component.test.tsx`)
- E2E tests are in `tests/e2e/`
- Test setup is in `src/test/setup.ts`

## Building

### Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint |
| `npm run test` | Run unit tests |
| `npm run test:coverage` | Run tests with coverage report |
| `npm run test:e2e` | Run Playwright E2E tests |

### Production Build

```bash
npm run build
```

Output is generated in the `dist/` directory.

### Docker Build

```bash
docker build -t supplemart-frontend .
```

The Dockerfile uses:
1. Node.js image for building
2. Nginx for serving static files in production
