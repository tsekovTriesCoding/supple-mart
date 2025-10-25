package app.product.service;

import app.product.dto.ProductDTO;
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

        List<ProductDTO> productDTOs = productMapper.toDTOList(productPage.getContent());

        return ProductPageResponse.builder()
                .products(productDTOs)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .size(productPage.getSize())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .build();
    }

    public List<Category> getAllCategories() {
        return Arrays.asList(Category.values());
    }
}
