# SuppleMart

SuppleMart is a full-stack e-commerce application built with modern technologies. The platform provides a complete online shopping experience with product browsing, shopping cart functionality, secure payments, user authentication, and a comprehensive admin dashboard.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Deployment](#deployment)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Contributing](#contributing)
- [License](#license)

## Overview

SuppleMart is designed as a learning project demonstrating enterprise-level application development practices. It showcases:

- Clean architecture with separation of concerns
- Secure authentication and authorization mechanisms
- Payment processing integration
- Cloud deployment with Infrastructure as Code
- Comprehensive testing strategies
- CI/CD automation

## Architecture

The application follows a client-server architecture with clear separation between the frontend and backend:

```
+------------------------------------------------------------------+
|                         Client Layer                              |
|  +--------------------------------------------------------------+ |
|  |                    React Application                          | |
|  |  (TypeScript, TanStack Query, React Router, Tailwind CSS)    | |
|  +--------------------------------------------------------------+ |
+------------------------------------------------------------------+
                              |
                              | HTTPS / REST API
                              v
+------------------------------------------------------------------+
|                         Server Layer                              |
|  +--------------------------------------------------------------+ |
|  |                  Spring Boot Application                      | |
|  |    (Java 21, Spring Security, Spring Data JPA, OAuth2)       | |
|  +--------------------------------------------------------------+ |
+------------------------------------------------------------------+
                              |
              +---------------+---------------+
              v               v               v
        +----------+   +----------+   +--------------+
        |  MySQL   |   | Cloudinary|   |    Stripe   |
        | Database |   |  (Images) |   |  (Payments) |
        +----------+   +----------+   +--------------+
```

## Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 4.0.1 | Application framework |
| Spring Security | 6.x | Authentication and authorization |
| Spring Data JPA | 3.x | Data persistence |
| Hibernate Envers | 6.x | Entity auditing |
| Flyway | 11.x | Database migrations |
| MySQL | 8.0 | Relational database |
| JWT (jjwt) | 0.12.6 | Token-based authentication |
| MapStruct | 1.6.3 | Object mapping |
| Lombok | Latest | Boilerplate reduction |
| Caffeine | 3.1.8 | In-memory caching |
| Stripe Java | 30.2.0 | Payment processing |
| Cloudinary | 1.39.0 | Image management |
| SpringDoc OpenAPI | 2.8.4 | API documentation |
| Micrometer Prometheus | Latest | Metrics collection |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.1.1 | UI framework |
| TypeScript | 5.x | Type-safe JavaScript |
| Vite | 6.x | Build tool |
| TanStack Query | 5.90.5 | Server state management |
| React Router | 7.9.4 | Client-side routing |
| React Hook Form | 7.65.0 | Form handling |
| Tailwind CSS | 4.x | Utility-first CSS |
| Axios | 1.12.2 | HTTP client |
| Stripe React | 5.3.0 | Payment UI components |
| Lucide React | 0.546.0 | Icon library |

### DevOps and Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Local development orchestration |
| GitHub Actions | CI/CD pipeline |
| Terraform | Infrastructure as Code |
| Azure Container Apps | Cloud hosting |
| Azure Container Registry | Container image storage |
| Prometheus | Metrics collection |
| Grafana | Metrics visualization |

## Features

### Customer Features
- **Product Browsing**: Search, filter, and sort products by category, price, and availability
- **Product Details**: View detailed product information, images, and customer reviews
- **Shopping Cart**: Add, update, and remove items with real-time price calculation
- **Wishlist**: Save products for later purchase
- **Secure Checkout**: Stripe-powered payment processing
- **Order Management**: View order history and track order status
- **User Authentication**: Email/password registration and login
- **OAuth2 Login**: Sign in with Google or GitHub accounts
- **Profile Management**: Update personal information and change password
- **Notification Preferences**: Configure email notification settings
- **Privacy Settings**: Manage data privacy and consent options
- **Product Reviews**: Rate and review purchased products

### Admin Features
- **Dashboard**: View key metrics and statistics
- **Product Management**: Create, update, and delete products with image upload
- **Order Management**: View and update order statuses
- **User Management**: View user accounts and manage roles
- **Cache Management**: Monitor and clear application caches
- **Audit History**: Track changes to products with revision history

### Technical Features
- **JWT Authentication**: Secure token-based authentication with refresh tokens
- **Role-Based Access Control**: Admin and customer role separation
- **Input Validation**: Comprehensive request validation
- **Error Handling**: Global exception handling with meaningful error responses
- **Database Migrations**: Version-controlled schema management with Flyway
- **Caching**: Multi-level caching for improved performance
- **Email Notifications**: Transactional emails for orders, shipping, and promotions
- **Scheduled Tasks**: Automated jobs for abandoned carts, low stock alerts, and reports
- **API Documentation**: Interactive Swagger UI documentation
- **Health Monitoring**: Actuator endpoints for application health checks
- **Metrics Collection**: Prometheus-compatible metrics for monitoring

## Project Structure

```
supple-mart/
|-- backend/                    # Spring Boot application
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/app/
|   |   |   |   |-- admin/      # Admin-specific functionality
|   |   |   |   |-- cart/       # Shopping cart domain
|   |   |   |   |-- cartitem/   # Cart item domain
|   |   |   |   |-- cloudinary/ # Image upload service
|   |   |   |   |-- config/     # Application configuration
|   |   |   |   |-- contact/    # Contact form handling
|   |   |   |   |-- exception/  # Global exception handling
|   |   |   |   |-- monitoring/ # Health indicators
|   |   |   |   |-- notification/ # Email notifications
|   |   |   |   |-- order/      # Order management
|   |   |   |   |-- payment/    # Stripe integration
|   |   |   |   |-- privacy/    # Privacy settings
|   |   |   |   |-- product/    # Product catalog
|   |   |   |   |-- review/     # Product reviews
|   |   |   |   |-- scheduling/ # Scheduled tasks
|   |   |   |   |-- security/   # Authentication/Authorization
|   |   |   |   |-- user/       # User management
|   |   |   |   |-- validation/ # Custom validators
|   |   |   |   |-- web/        # REST controllers
|   |   |   |   +-- wishlist/   # Wishlist functionality
|   |   |   +-- resources/
|   |   |       |-- db/migration/  # Flyway migrations
|   |   |       +-- templates/     # Email templates
|   |   +-- test/               # Test sources
|   |-- Dockerfile
|   +-- build.gradle
|
|-- frontend/                   # React application
|   |-- src/
|   |   |-- components/         # Reusable UI components
|   |   |-- hooks/              # Custom React hooks
|   |   |-- lib/                # API client and utilities
|   |   |-- pages/              # Page components
|   |   |   +-- admin/          # Admin pages
|   |   |-- reducers/           # State reducers
|   |   |-- styles/             # CSS styles
|   |   |-- types/              # TypeScript definitions
|   |   +-- utils/              # Utility functions
|   |-- tests/                  # E2E tests
|   |-- Dockerfile
|   +-- package.json
|
|-- infra/                      # Terraform infrastructure
|   |-- main.tf
|   +-- outputs.tf
|
|-- monitoring/                 # Prometheus and Grafana
|   |-- grafana/
|   |-- prometheus/
|   +-- docker-compose.yml
|
|-- .github/
|   +-- workflows/
|       +-- ci-cd.yml           # CI/CD pipeline
|
+-- docker-compose.yml          # Local development setup
```

## Prerequisites

- Java 21 or higher
- Node.js 20 or higher
- Docker and Docker Compose
- MySQL 8.0 (or use Docker)
- Git

## Getting Started

### Local Development with Docker Compose

1. Clone the repository:
   ```bash
   git clone https://github.com/tsekovTriesCoding/supple-mart.git
   cd supple-mart
   ```

2. Create a `.env` file in the root directory with required environment variables (see Environment Variables section).

3. Start all services:
   ```bash
   docker-compose up -d
   ```

4. Access the applications:
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Manual Setup

#### Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Create a `.env` file with required variables.

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

#### Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a `.env` file:
   ```
   VITE_API_URL=http://localhost:8080/api/
   VITE_BACKEND_URL=http://localhost:8080
   VITE_STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

## Environment Variables

### Backend

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_USERNAME` | MySQL database username | Yes |
| `DB_PASSWORD` | MySQL database password | Yes |
| `JWT_SECRET` | Secret key for JWT signing (min 256 bits) | Yes |
| `JWT_EXPIRATION` | Access token expiration in seconds | Yes |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration in seconds | Yes |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | Yes |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | Yes |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID | Yes |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret | Yes |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | Yes |
| `CLOUDINARY_API_KEY` | Cloudinary API key | Yes |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | Yes |
| `STRIPE_SECRET_KEY` | Stripe secret API key | Yes |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret | Yes |
| `MAIL_HOST` | SMTP server host | Yes |
| `MAIL_PORT` | SMTP server port | Yes |
| `MAIL_USERNAME` | SMTP username | Yes |
| `MAIL_PASSWORD` | SMTP password | Yes |
| `FRONTEND_URL` | Frontend application URL | No |

### Frontend

| Variable | Description | Required |
|----------|-------------|----------|
| `VITE_API_URL` | Backend API base URL | Yes |
| `VITE_BACKEND_URL` | Backend base URL (for OAuth) | Yes |
| `VITE_STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | Yes |

## Deployment

The application is deployed to Azure Container Apps using GitHub Actions and Terraform.

### CI/CD Pipeline

The pipeline is triggered on:
- Push to `main` branch
- Pull requests to `main` branch

Pipeline stages:
1. **Backend Tests**: Runs JUnit tests with Testcontainers
2. **Frontend Tests**: Runs Vitest unit tests
3. **Infrastructure**: Provisions Azure resources with Terraform
4. **Build and Deploy**: Builds Docker images and deploys to Azure Container Apps

### Infrastructure

The following Azure resources are provisioned:
- Resource Group
- Container Apps Environment
- Container Registry
- Log Analytics Workspace
- Application Insights
- Container Apps (Backend, Frontend, MySQL)

### Manual Deployment

1. Ensure Azure CLI is installed and authenticated.

2. Navigate to the infra directory:
   ```bash
   cd infra
   ```

3. Initialize Terraform:
   ```bash
   terraform init
   ```

4. Apply the configuration:
   ```bash
   terraform apply
   ```

## API Documentation

The API is documented using OpenAPI 3.0 specification with SpringDoc.

### Accessing Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### API Endpoints Overview

| Category | Base Path | Description |
|----------|-----------|-------------|
| Authentication | `/api/auth` | Login, register, refresh tokens |
| OAuth2 | `/api/oauth2` | OAuth2 provider information |
| Products | `/api/products` | Product catalog operations |
| Cart | `/api/cart` | Shopping cart management |
| Orders | `/api/orders` | Order management |
| Payments | `/api/payments` | Payment processing |
| Reviews | `/api/reviews` | Product reviews |
| Wishlist | `/api/wishlist` | Wishlist management |
| User | `/api/user` | User profile management |
| Contact | `/api/contact` | Contact form submission |
| Admin | `/api/admin` | Admin operations |

## Testing

### Backend Tests

Run all tests:
```bash
cd backend
./gradlew test
```

The backend uses:
- JUnit 5 for unit and integration tests
- Testcontainers for database integration tests
- MockMvc for controller tests

### Frontend Tests

Run unit tests:
```bash
cd frontend
npm run test
```

Run tests with coverage:
```bash
npm run test:coverage
```

Run E2E tests:
```bash
npm run test:e2e
```

The frontend uses:
- Vitest for unit tests
- React Testing Library for component tests
- Playwright for E2E tests

## Monitoring

### Local Monitoring Stack

Start Prometheus and Grafana:
```bash
cd monitoring
docker-compose up -d
```

Access:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (admin/admin)

### Production Monitoring

In Azure, Application Insights provides:
- Request tracing
- Performance monitoring
- Error tracking
- Custom metrics

### Health Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/prometheus` | Prometheus metrics |

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

### Code Style

- Backend: Follow standard Java conventions
- Frontend: ESLint configuration is provided

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.