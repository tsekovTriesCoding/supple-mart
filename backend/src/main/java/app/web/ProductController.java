package app.web;

import app.product.dto.ProductDetails;
import app.product.dto.ProductPageResponse;
import app.product.model.Category;
import app.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog and search endpoints")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get all products",
            description = "Retrieve paginated list of products with optional filters for search, category, price range, and sorting"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductPageResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ProductPageResponse> getAllProducts(
            @Parameter(description = "Search term for product name or description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by product category")
            @RequestParam(required = false) Category category,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Filter by active status (default: true)")
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Field to sort by (e.g., name, price, createdAt)")
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ) {
        ProductPageResponse products = productService.getAllProducts(
                search, category, minPrice, maxPrice, active, page, size, sortBy, sortDirection
        );

        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Get all categories",
            description = "Retrieve list of all available product categories"
    )
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories()
                .stream()
                .map(category -> category.name().toLowerCase())
                .toList();
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieve detailed information about a specific product including reviews"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductDetails.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetails> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id) {
        ProductDetails product = productService.getProductDetailsById(id);
        return ResponseEntity.ok(product);
    }
}
