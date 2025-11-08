package app.web;

import app.admin.dto.AdminProductPageResponse;
import app.admin.dto.CreateProductRequest;
import app.admin.dto.ImageUploadResponse;
import app.admin.dto.UpdateProductRequest;
import app.admin.service.AdminProductService;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
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
        AdminProductPageResponse response = adminProductService.getAllProductsForAdmin(
                search, category, minPrice, maxPrice, active, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductDetailsDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Admin: Creating new product: {}", request.getName());
        ProductDetailsDTO product = adminProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("Admin: Updating product with ID: {}", id);
        ProductDetailsDTO product = adminProductService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("Admin: Deleting product with ID: {}", id);
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadProductImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Admin: Uploading product image");
        ImageUploadResponse response = adminProductService.uploadProductImage(file);
        return ResponseEntity.ok(response);
    }
}

