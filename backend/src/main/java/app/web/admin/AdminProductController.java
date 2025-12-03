package app.web.admin;

import app.admin.dto.AdminProductPageResponse;
import app.admin.dto.CreateProductRequest;
import app.admin.dto.ImageUploadResponse;
import app.admin.dto.UpdateProductRequest;
import app.admin.service.AdminProductService;
import app.product.dto.ProductDetails;
import app.product.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Products", description = "Product management endpoints (requires ADMIN role)")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "Get all products", description = "Retrieve paginated list of all products with filters (includes sales data)")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @GetMapping
    public ResponseEntity<AdminProductPageResponse> getAllProducts(
            @Parameter(description = "Search by product name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category") @RequestParam(required = false) Category category,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("Admin: Fetching all products - page: {}, size: {}", page, size);
        AdminProductPageResponse response = adminProductService.getAllProductsForAdmin(
                search, category, minPrice, maxPrice, active, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create product", description = "Create a new product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductDetails.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    @PostMapping
    public ResponseEntity<ProductDetails> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Admin: Creating new product: {}", request.getName());
        ProductDetails product = adminProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "Update product", description = "Update an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDetails> updateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("Admin: Updating product with ID: {}", id);
        ProductDetails product = adminProductService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Delete product", description = "Delete a product (not allowed if product has orders)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Product has existing orders"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product ID") @PathVariable UUID id) {
        log.info("Admin: Deleting product with ID: {}", id);
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload product image", description = "Upload an image for a product (Cloudinary)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ImageUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file")
    })
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadProductImage(
            @Parameter(description = "Image file") @RequestParam("file") MultipartFile file
    ) {
        log.info("Admin: Uploading product image");
        ImageUploadResponse response = adminProductService.uploadProductImage(file);
        return ResponseEntity.ok(response);
    }
}

