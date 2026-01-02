# SuppleMart Backend

The SuppleMart backend is a Spring Boot application that provides RESTful APIs for the e-commerce platform. It handles product management, user authentication, order processing, payment integration, and administrative functions.

## Table of Contents

- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Database](#database)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Caching](#caching)
- [Scheduled Tasks](#scheduled-tasks)
- [Testing](#testing)
- [Building](#building)

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 4.0.1 | Application framework |
| Spring Security | 6.x | Security framework |
| Spring Data JPA | 3.x | Data access layer |
| Hibernate | 6.x | ORM framework |
| Hibernate Envers | 6.x | Entity auditing |
| Flyway | 11.x | Database migrations |
| MySQL | 8.0 | Database |
| jjwt | 0.12.6 | JWT handling |
| MapStruct | 1.6.3 | DTO mapping |
| Lombok | Latest | Boilerplate reduction |
| Caffeine | 3.1.8 | In-memory caching |
| Stripe Java | 30.2.0 | Payment processing |
| Cloudinary | 1.39.0 | Image storage |
| SpringDoc OpenAPI | 2.8.4 | API documentation |
| Micrometer | Latest | Metrics |
| Testcontainers | Latest | Integration testing |

## Architecture

The application follows a layered architecture pattern:

```
+------------------+
|   Controllers    |  REST API endpoints
+------------------+
         |
+------------------+
|    Services      |  Business logic
+------------------+
         |
+------------------+
|  Repositories    |  Data access
+------------------+
         |
+------------------+
|    Entities      |  Domain models
+------------------+
```

### Key Design Patterns

- **Repository Pattern**: Data access abstraction with Spring Data JPA
- **DTO Pattern**: Separate transfer objects for API communication
- **Service Layer Pattern**: Business logic encapsulation
- **Strategy Pattern**: Payment processing strategies
- **Factory Pattern**: Email notification creation

## Project Structure

The application uses a hybrid package structure combining feature-based organization with a centralized web layer for REST controllers.

```
src/main/java/app/
|-- admin/                      # Admin business logic
|   |-- dto/                    # Admin-specific DTOs
|   |-- mapper/                 # Admin mappers
|   +-- service/                # Admin services
|
|-- cart/                       # Shopping cart feature
|   |-- dto/                    # Cart DTOs
|   |-- mapper/                 # Cart mappers
|   |-- model/                  # Cart entity
|   |-- repository/             # Cart data access
|   +-- service/                # Cart business logic
|
|-- cartitem/                   # Cart item feature
|   |-- model/                  # CartItem entity
|   +-- repository/             # CartItem data access
|
|-- cloudinary/                 # Image management
|   |-- config/                 # Cloudinary configuration
|   +-- service/                # Upload service
|
|-- config/                     # Application configuration
|   |-- CacheConfig.java
|   |-- OpenApiConfig.java
|   |-- SchedulingConfig.java
|   +-- WebMvcConfig.java
|
|-- contact/                    # Contact form feature
|   |-- dto/                    # Contact DTOs
|   +-- service/                # Contact email service
|
|-- exception/                  # Exception handling
|   |-- GlobalExceptionHandler.java
|   +-- [Custom exceptions]
|
|-- monitoring/                 # Health indicators
|   +-- DatabaseHealthIndicator.java
|
|-- notification/               # Notification feature
|   |-- dto/                    # Notification DTOs
|   |-- model/                  # NotificationPreference entity
|   |-- repository/             # Data access
|   +-- service/                # Email services
|
|-- order/                      # Order feature
|   |-- dto/                    # Order DTOs
|   |-- mapper/                 # Order mappers
|   |-- model/                  # Order, OrderItem, ShippingAddress entities
|   |-- repository/             # Data access
|   +-- service/                # Order processing
|
|-- payment/                    # Payment feature
|   |-- dto/                    # Payment DTOs
|   +-- service/                # Stripe integration
|
|-- privacy/                    # Privacy settings feature
|   |-- dto/                    # Privacy DTOs
|   |-- model/                  # PrivacySettings entity
|   |-- repository/             # Data access
|   +-- service/                # Privacy logic
|
|-- product/                    # Product catalog feature
|   |-- dto/                    # Product DTOs
|   |-- mapper/                 # Product mappers
|   |-- model/                  # Product entity
|   |-- repository/             # Data access
|   |-- service/                # Product logic
|   +-- specification/          # JPA Specifications for filtering
|
|-- review/                     # Review feature
|   |-- dto/                    # Review DTOs
|   |-- mapper/                 # Review mappers
|   |-- model/                  # Review entity
|   |-- repository/             # Data access
|   +-- service/                # Review logic
|
|-- scheduling/                 # Scheduled tasks
|   +-- ScheduledTasks.java
|
|-- security/                   # Security configuration
|   |-- auth/                   # Authentication DTOs and services
|   |-- config/                 # Security configuration
|   |-- jwt/                    # JWT utilities and filters
|   +-- oauth2/                 # OAuth2 handlers
|
|-- user/                       # User feature
|   |-- dto/                    # User DTOs
|   |-- mapper/                 # User mappers
|   |-- model/                  # User, Role entities
|   |-- repository/             # Data access
|   +-- service/                # User logic
|
|-- validation/                 # Custom validators
|   |-- UniqueEmail.java
|   +-- PasswordMatch.java
|
|-- web/                        # REST Controllers (centralized)
|   |-- AuthController.java
|   |-- CartController.java
|   |-- ContactController.java
|   |-- NotificationPreferencesController.java
|   |-- OAuth2Controller.java
|   |-- OrderController.java
|   |-- PaymentController.java
|   |-- PrivacySettingsController.java
|   |-- ProductController.java
|   |-- ReviewController.java
|   |-- UserController.java
|   |-- WishlistController.java
|   +-- admin/                  # Admin REST Controllers
|       |-- AdminAuditController.java
|       |-- AdminCacheController.java
|       |-- AdminDashboardController.java
|       |-- AdminOrderController.java
|       |-- AdminProductController.java
|       +-- AdminUserController.java
|
+-- wishlist/                   # Wishlist feature
    |-- dto/                    # Wishlist DTOs
    |-- mapper/                 # Wishlist mappers
    |-- model/                  # Wishlist, WishlistItem entities
    |-- repository/             # Data access
    +-- service/                # Wishlist logic
```

## Getting Started

### Prerequisites

- Java 21
- MySQL 8.0 (or Docker)
- Gradle 8.x (wrapper included)

### Running with Docker

1. Build the Docker image:
   ```bash
   docker build -t supplemart-backend .
   ```

2. Run with Docker Compose from the project root:
   ```bash
   docker-compose up -d
   ```

### Running Locally

1. Ensure MySQL is running and create the database:
   ```sql
   CREATE DATABASE supple_mart;
   ```

2. Set environment variables (see Configuration section).

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

4. The API will be available at http://localhost:8080

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `default` |
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `supple_mart` |
| `DB_USERNAME` | Database username | - |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing key (256+ bits) | - |
| `JWT_EXPIRATION` | Access token TTL (seconds) | `86400` |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (seconds) | `604800` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 secret | - |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID | - |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 secret | - |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | - |
| `CLOUDINARY_API_KEY` | Cloudinary API key | - |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | - |
| `STRIPE_SECRET_KEY` | Stripe secret key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook secret | - |
| `MAIL_HOST` | SMTP host | - |
| `MAIL_PORT` | SMTP port | - |
| `MAIL_USERNAME` | SMTP username | - |
| `MAIL_PASSWORD` | SMTP password | - |
| `FRONTEND_URL` | Frontend URL for CORS | `http://localhost:5173` |

### Application Properties

Key configuration files:
- `application.properties` - Main configuration
- `contact-subjects.properties` - Contact form subjects

## Database

### Schema Management

Database schema is managed using Flyway migrations located in `src/main/resources/db/migration/`:

| Migration | Description |
|-----------|-------------|
| V1 | Initial schema with products table and indexes |
| V2 | User and role tables |
| V3 | Cart and cart_items tables |
| V4 | Orders, order_items, and shipping_address tables |
| V5 | Reviews table |
| V6 | Wishlist and wishlist_items tables |
| V7 | Hibernate Envers audit tables |
| V8 | Notification preferences, privacy settings, refresh tokens |

### Entity Relationships

```
User 1----* Cart
User 1----* Order
User 1----* Review
User 1----* Wishlist
User *----* Role
Cart 1----* CartItem
Order 1----* OrderItem
Order 1----1 ShippingAddress
Wishlist 1----* WishlistItem
Product 1----* CartItem
Product 1----* OrderItem
Product 1----* Review
Product 1----* WishlistItem
```

### Auditing

Product entities are audited using Hibernate Envers, tracking:
- Creation and modification timestamps
- User who made changes
- Previous values for all fields
- Revision history

## Authentication

### JWT Authentication

The application uses JWT tokens for stateless authentication:

1. **Access Token**: Short-lived token (default 24 hours) for API access
2. **Refresh Token**: Long-lived token (default 7 days) for obtaining new access tokens

Token flow:
```
1. User logs in with credentials
2. Server validates and returns access + refresh tokens
3. Client includes access token in Authorization header
4. When access token expires, use refresh token to get new pair
5. On logout, refresh token is revoked
```

### OAuth2 Authentication

Supports authentication via:
- **Google**: OAuth2 with OpenID Connect
- **GitHub**: OAuth2 authorization code flow

OAuth2 flow:
```
1. Frontend redirects to /oauth2/authorization/{provider}
2. User authenticates with provider
3. Server receives callback with authorization code
4. Server exchanges code for provider tokens
5. Server creates/updates user and issues JWT
6. Server redirects to frontend with tokens
```

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/logout` | User logout |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/oauth2/providers` | Get OAuth2 providers |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get product details |
| GET | `/api/products/search` | Search products |
| GET | `/api/products/categories` | List categories |

### Cart
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | Get user's cart |
| POST | `/api/cart/items` | Add item to cart |
| PUT | `/api/cart/items/{id}` | Update item quantity |
| DELETE | `/api/cart/items/{id}` | Remove item |
| DELETE | `/api/cart` | Clear cart |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders` | List user's orders |
| GET | `/api/orders/{id}` | Get order details |
| POST | `/api/orders` | Create order |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/create-payment-intent` | Create Stripe payment |
| POST | `/api/payments/webhook` | Stripe webhook handler |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reviews/product/{id}` | Get product reviews |
| POST | `/api/reviews` | Create review |
| PUT | `/api/reviews/{id}` | Update review |
| DELETE | `/api/reviews/{id}` | Delete review |

### Wishlist
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wishlist` | Get user's wishlist |
| POST | `/api/wishlist/items` | Add to wishlist |
| DELETE | `/api/wishlist/items/{id}` | Remove from wishlist |

### User Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/user/profile` | Get user profile |
| PUT | `/api/user/profile` | Update profile |
| PUT | `/api/user/password` | Change password |

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard/stats` | Dashboard statistics |
| GET | `/api/admin/products` | List all products |
| POST | `/api/admin/products` | Create product |
| PUT | `/api/admin/products/{id}` | Update product |
| DELETE | `/api/admin/products/{id}` | Delete product |
| GET | `/api/admin/orders` | List all orders |
| PUT | `/api/admin/orders/{id}/status` | Update order status |
| GET | `/api/admin/users` | List all users |
| PUT | `/api/admin/users/{id}/role` | Update user role |
| GET | `/api/admin/cache/stats` | Cache statistics |
| DELETE | `/api/admin/cache/{name}` | Clear specific cache |
| GET | `/api/admin/audit/products/{id}` | Product audit history |

### Contact
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/contact` | Submit contact form |
| GET | `/api/contact/subjects` | Get contact subjects |

## Caching

The application uses Caffeine for in-memory caching:

### Cache Configurations

| Cache Name | TTL | Max Size | Purpose |
|------------|-----|----------|---------|
| `products` | 10 min | 500 | Product listings |
| `product` | 10 min | 1000 | Individual products |
| `categories` | 30 min | 100 | Product categories |
| `contactSubjects` | 24 hrs | 50 | Contact subjects |

### Cache Eviction

Caches are automatically evicted when:
- Products are created, updated, or deleted
- Admin clears cache manually via API

## Scheduled Tasks

| Task | Schedule | Description |
|------|----------|-------------|
| Daily Report | 9:00 AM | Generate daily statistics |
| Abandoned Cart | Every 6 hours | Send reminders for abandoned carts |
| Low Stock Alert | Every hour | Alert admins about low stock |
| Promotional Email | Mondays 10:00 AM | Send weekly promotions |
| Token Cleanup | Every 6 hours | Remove expired refresh tokens |

Scheduling can be enabled/disabled via:
```properties
scheduling.enabled=true
```

## Testing

### Running Tests

Run all tests:
```bash
./gradlew test
```

Run with coverage:
```bash
./gradlew test jacocoTestReport
```

### Test Categories

- **Unit Tests**: Service layer tests with mocked dependencies
- **Integration Tests**: Repository tests with Testcontainers
- **Controller Tests**: API tests with MockMvc

### Testcontainers

Integration tests use Testcontainers for MySQL:
```java
@Testcontainers
class ProductRepositoryTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
}
```

## Building

### Build JAR

```bash
./gradlew build
```

### Build Docker Image

```bash
docker build -t supplemart-backend .
```

### Gradle Tasks

| Task | Description |
|------|-------------|
| `./gradlew bootRun` | Run application |
| `./gradlew build` | Build JAR |
| `./gradlew test` | Run tests |
| `./gradlew clean` | Clean build artifacts |
| `./gradlew bootJar` | Build executable JAR |

## API Documentation

Access interactive API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
