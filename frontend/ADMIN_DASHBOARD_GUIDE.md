# 🛡️ Admin Dashboard - Implementation Guide

## Overview

I've created a comprehensive Admin Dashboard for your Supple Mart e-commerce application with full product management capabilities, including image uploads.

---

## ✨ Features Implemented

### 1. **Admin Authentication & Authorization**
- ✅ Role-based access control (ADMIN/CUSTOMER)
- ✅ Protected admin routes
- ✅ Automatic redirect if not admin
- ✅ Admin Panel link in user dropdown (visible only to admins)

### 2. **Admin Dashboard**
- ✅ Beautiful sidebar navigation
- ✅ Dashboard stats overview:
  - Total Products
  - Total Orders
  - Total Revenue
  - Total Customers
  - Low Stock Alerts
- ✅ Quick action cards
- ✅ Responsive design

### 3. **Product Management (Full CRUD)**
- ✅ **Create**: Add new products with image upload
- ✅ **Read**: View all products in a table
- ✅ **Update**: Edit existing products
- ✅ **Delete**: Remove products
- ✅ **Image Upload**: Upload product images (max 5MB)
- ✅ **Search**: Search products by name
- ✅ **Filter**: Filter by category
- ✅ **Pagination**: Navigate through product pages
- ✅ **Stock Status**: Visual indicators for stock levels

### 4. **Order Management** (Placeholder)
- 📝 Ready for implementation
- Order status updates
- Order details view

### 5. **User Management** (Placeholder)
- 📝 Ready for implementation
- View all users
- Manage roles

---

## 📁 Files Created

```
frontend/src/
├── types/
│   └── auth.ts                      # Updated with UserRole type
├── lib/api/
│   └── admin.ts                     # Admin API endpoints
├── components/
│   └── AdminRoute.tsx               # Protected route component
├── pages/admin/
│   ├── AdminDashboard.tsx           # Main dashboard
│   ├── AdminProducts.tsx            # Product management
│   ├── AdminOrders.tsx              # Orders placeholder
│   └── AdminUsers.tsx               # Users placeholder
└── App.tsx                          # Updated with admin routes
```

---

## 🚀 How to Access

1. **Login as Admin**
   - The user must have `role: "ADMIN"` in the backend
   - Login credentials: (use your admin user)

2. **Navigate to Admin Panel**
   - Click on your user avatar in the header
   - Click "Admin Panel" (blue link)
   - Or go directly to: `http://localhost:5173/admin`

3. **Non-admin users will be redirected to home page**

---

## 🎨 Admin Dashboard Features

### Dashboard Home (`/admin`)
- View statistics at a glance
- Low stock warnings
- Quick navigation cards

### Product Management (`/admin/products`)

#### Add Product:
1. Click "Add Product" button
2. Upload product image (optional)
3. Fill in product details:
   - Name (required)
   - Description (required)
   - Category (required)
   - Price (required)
   - Original Price (optional, for sales)
   - Stock Quantity (required)
4. Click "Add Product"

#### Edit Product:
1. Click edit icon (pencil) on any product row
2. Modify fields
3. Click "Update Product"

#### Delete Product:
1. Click delete icon (trash) on any product row
2. Confirm deletion

#### Features:
- **Search**: Type in search box to find products
- **Filter**: Select category from dropdown
- **Pagination**: Navigate between pages
- **Stock Indicators**:
  - 🟢 Green: Stock > 10
  - 🟡 Yellow: Stock 1-10
  - 🔴 Red: Out of stock

---

## 🔧 Backend Requirements

You need to implement these endpoints in your Spring Boot backend:

### Dashboard Stats
```java
GET /api/admin/dashboard/stats
Response: {
  totalProducts: number,
  totalOrders: number,
  totalRevenue: number,
  totalCustomers: number,
  lowStockProducts: number
}
```

### Product Management
```java
// Get all products (with pagination)
GET /api/admin/products?page=0&size=10&search=...&category=...

// Create product
POST /api/admin/products
Body: CreateProductRequest

// Update product
PUT /api/admin/products/{id}
Body: UpdateProductRequest

// Delete product
DELETE /api/admin/products/{id}

// Upload image
POST /api/admin/products/upload-image
Content-Type: multipart/form-data
Body: { file: File }
Response: { imageUrl: string }
```

### Order Management
```java
// Get all orders
GET /api/admin/orders?page=0&size=10&status=...

// Update order status
PATCH /api/admin/orders/{id}/status
Body: { status: string }
```

### User Management
```java
// Get all users
GET /api/admin/users?page=0&size=10&search=...
```

### Security
```java
// Add @PreAuthorize annotation to all admin endpoints
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/products")
public ResponseEntity<Page<Product>> getAllProducts(...) {
    // ...
}
```

---

## 🎯 User Role Setup

Make sure your User entity has a role field:

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // ADMIN or CUSTOMER
    
    // getters and setters
}

public enum UserRole {
    CUSTOMER,
    ADMIN
}
```

---

## 📝 Image Upload Implementation

### Frontend
The frontend sends a `multipart/form-data` request with the image file.

### Backend Example
```java
@PostMapping("/upload-image")
public ResponseEntity<Map<String, String>> uploadImage(
    @RequestParam("file") MultipartFile file
) {
    // Save file to cloud storage (AWS S3, Cloudinary, etc.)
    // or local storage
    String imageUrl = imageService.uploadImage(file);
    
    return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
}
```

---

## 🔒 Security Checklist

- ✅ Admin routes protected with `AdminRoute` component
- ✅ User role checked on frontend
- ⚠️ **IMPORTANT**: Implement backend authorization
  - Use `@PreAuthorize("hasRole('ADMIN')")`
  - Never trust frontend-only checks
  - Validate user role in JWT token

---

## 🎨 UI/UX Features

- **Dark Theme**: Consistent with your app design
- **Responsive**: Works on mobile and desktop
- **Smooth Animations**: Hover effects and transitions
- **Loading States**: Skeleton loaders and spinners
- **Error Handling**: Backend error messages displayed
- **Confirmation Dialogs**: Prevent accidental deletions
- **Toast Notifications**: Success/error feedback

---

## 📊 Product Form Validation

- **Name**: Required
- **Description**: Required
- **Category**: Required (dropdown)
- **Price**: Required, number input
- **Original Price**: Optional, for sale prices
- **Stock**: Required, integer
- **Image**: Optional, max 5MB, image files only

---

## 🚀 Next Steps

### Priority 1: Backend Implementation
1. Create admin controller with all endpoints
2. Implement role-based authorization
3. Add image upload functionality
4. Test API endpoints with Postman

### Priority 2: Testing
1. Test admin login flow
2. Test product CRUD operations
3. Test image uploads
4. Test access control (non-admin should not access)

### Priority 3: Enhancements
1. Implement Order Management page
2. Implement User Management page
3. Add analytics charts
4. Add bulk operations (CSV import/export)
5. Add audit logs

---

## 💡 Tips

1. **Testing Admin Access**:
   - Manually set a user's role to "ADMIN" in your database
   - Login with that user
   - Admin Panel link should appear in user dropdown

2. **Image Storage**:
   - For development: Save to `/uploads` folder
   - For production: Use cloud storage (AWS S3, Cloudinary)

3. **Security**:
   - Always verify role on backend
   - Use JWT with role claim
   - Rate limit admin endpoints

---

## 🐛 Troubleshooting

**Q: Admin Panel link doesn't show**
- A: Make sure user.role === 'ADMIN' in localStorage

**Q: Getting 403 Forbidden**
- A: Backend needs to implement role-based authorization

**Q: Image upload fails**
- A: Check file size (<5MB) and backend endpoint

**Q: Can't access /admin routes**
- A: Make sure you're logged in as admin

---

## 📞 Support

If you need help implementing any of these features or have questions about the admin panel, feel free to ask!

---

**Happy Admin Panel Managing! 🎉**
