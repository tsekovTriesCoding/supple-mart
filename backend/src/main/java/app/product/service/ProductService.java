package app.product.service;

import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.product.dto.ProductDetails;
import app.product.dto.ProductPageResponse;
import app.product.mapper.ProductMapper;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductPageResponse getAllProducts(
            String search,
            Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Use JPA Specification for dynamic, composable filtering
        Specification<Product> spec = ProductSpecification.withFilters(
                search, category, minPrice, maxPrice, active
        );

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productMapper.toPageResponse(productPage);
    }

    public List<Category> getAllCategories() {
        return Arrays.asList(Category.values());
    }

    public ProductDetails getProductDetailsById(UUID id) {
        Product product = productRepository.findByIdWithReviews(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        return productMapper.toProductDetails(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
    }

    public Long getTotalProductsCount() {
        return productRepository.count();
    }

    public Long getLowStockProductsCount() {
        return productRepository.countLowStockProducts();
    }

    public Page<Product> getProductsWithFilters(
            String search,
            Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Pageable pageable
    ) {
        Specification<Product> spec = ProductSpecification.withFilters(
                search, category, minPrice, maxPrice, active
        );
        return productRepository.findAll(spec, pageable);
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = getProductById(id);

        if (product.getOrderItems() != null && !product.getOrderItems().isEmpty()) {
            throw new BadRequestException("Cannot delete product with existing orders. Consider marking it as inactive instead.");
        }

        productRepository.delete(product);
    }

    /**
     * Reserves inventory when an order is created
     */
    @Transactional
    public void reserveInventory(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName()
                    + ". Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);

        if (product.getStockQuantity() == 0) {
            log.info("Product {} is now out of stock", productId);
        }

        productRepository.save(product);
        log.debug("Reserved {} units of product {}. Remaining stock: {}",
                quantity, productId, product.getStockQuantity());
    }

    /**
     * Releases inventory when an order is cancelled
     */
    @Transactional
    public void releaseInventory(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setStockQuantity(product.getStockQuantity() + quantity);

        if (product.getStockQuantity() > 0) {
            log.info("Product {} restocked. Current stock: {}", productId, product.getStockQuantity());
        }

        productRepository.save(product);
        log.debug("Released {} units of product {}. Current stock: {}",
                quantity, productId, product.getStockQuantity());
    }
}
