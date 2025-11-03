package app.product.service;

import app.exception.ResourceNotFoundException;
import app.product.dto.ProductDetailsDTO;
import app.product.dto.ProductPageResponse;
import app.product.mapper.ProductMapper;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

        Page<Product> productPage = productRepository.findProductsWithFilters(
                search, category, minPrice, maxPrice, active, pageable
        );

        return productMapper.toPageResponse(productPage);
    }

    public List<Category> getAllCategories() {
        return Arrays.asList(Category.values());
    }

    public ProductDetailsDTO getProductDetailsById(UUID id) {
        Product product = productRepository.findByIdWithReviews(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        return productMapper.toDetailsDTO(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
    }
}
