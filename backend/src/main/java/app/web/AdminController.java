package app.web;

import app.admin.dto.*;
import app.admin.service.AdminService;
import app.order.dto.OrderDTO;
import app.product.dto.ProductDetailsDTO;
import app.product.model.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        log.info("Admin: Fetching dashboard statistics");
        DashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/products")
    public ResponseEntity<AdminProductPageResponse> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("Admin: Fetching all products - page: {}, size: {}", page, size);
        AdminProductPageResponse response = adminService.getAllProductsForAdmin(
                search, category, minPrice, maxPrice, active, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDetailsDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Admin: Creating new product: {}", request.getName());
        ProductDetailsDTO product = adminService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDetailsDTO> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("Admin: Updating product with ID: {}", id);
        ProductDetailsDTO product = adminService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("Admin: Deleting product with ID: {}", id);
        adminService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/products/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadProductImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Admin: Uploading product image");
        ImageUploadResponse response = adminService.uploadProductImage(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<AdminOrdersResponse> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("Admin: Fetching all orders - page: {}, limit: {}", page, limit);
        AdminOrdersResponse response = adminService.getAllOrders(status, startDate, endDate, page, limit);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        log.info("Admin: Updating order {} status to {}", orderId, request.getStatus());
        OrderDTO order = adminService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/users")
    public ResponseEntity<AdminUsersResponse> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Admin: Fetching all users - page: {}, size: {}", page, size);
        AdminUsersResponse response = adminService.getAllUsers(search, page, size);
        return ResponseEntity.ok(response);
    }
}
