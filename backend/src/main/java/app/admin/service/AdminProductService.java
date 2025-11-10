package app.admin.service;

import app.admin.dto.AdminProductPageResponse;
import app.admin.dto.CreateProductRequest;
import app.admin.dto.ImageUploadResponse;
import app.admin.dto.UpdateProductRequest;
import app.admin.mapper.AdminMapper;
import app.order.service.OrderService;
import app.product.dto.ProductDetailsDTO;
import app.product.mapper.ProductMapper;
import app.product.model.Category;
import app.product.model.Product;
import app.product.service.ProductService;
import app.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductMapper productMapper;
    private final AdminMapper adminMapper;
    private final CloudinaryService cloudinaryService;

    private static final String CLOUDINARY_FOLDER = "supplemart/products";

    public AdminProductPageResponse getAllProductsForAdmin(
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
        log.info("Fetching products for admin - page: {}, size: {}", page, size);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Product> productPage = productService.getProductsWithFilters(
                search, category, minPrice, maxPrice, active, pageable
        );

        Map<UUID, Integer> salesMap = new HashMap<>();
        for (Product product : productPage.getContent()) {
            Integer totalSales = orderService.getTotalSalesByProductId(product.getId());
            salesMap.put(product.getId(), totalSales);
        }

        return adminMapper.toAdminProductPageResponse(productPage, salesMap);
    }

    @Transactional
    public ProductDetailsDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        Product product = adminMapper.toProductEntity(request);
        Product savedProduct = productService.createProduct(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toDetailsDTO(savedProduct);
    }

    @Transactional
    public ProductDetailsDTO updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productService.getProductById(id);

        String oldImageUrl = product.getImageUrl();
        String newImageUrl = request.getImageUrl();

        // If the image URL has changed and both are Cloudinary URLs, delete the old image
        if (oldImageUrl != null && !oldImageUrl.isEmpty() &&
            newImageUrl != null && !newImageUrl.equals(oldImageUrl)) {
            deleteImageFromCloudinary(id, oldImageUrl);
        }

        adminMapper.updateProductEntity(product, request);
        Product updatedProduct = productService.updateProduct(product);

        log.info("Product updated successfully: {}", id);
        return productMapper.toDetailsDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productService.getProductById(id);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            deleteImageFromCloudinary(id, product.getImageUrl());
        }

        productService.deleteProduct(id);
        log.info("Product deleted successfully: {}", id);
    }

    @Transactional
    public ImageUploadResponse uploadProductImage(MultipartFile file) {
        log.info("Uploading product image to Cloudinary: {}", file.getOriginalFilename());

        String imageUrl = cloudinaryService.uploadImage(file, CLOUDINARY_FOLDER);

        log.info("Image uploaded successfully to Cloudinary: {}", imageUrl);

        return ImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .message("Image uploaded successfully")
                .build();
    }

    private void deleteImageFromCloudinary(UUID id, String oldImageUrl) {
        try {
            String publicId = cloudinaryService.extractPublicId(oldImageUrl);
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
                log.info("Old image deleted from Cloudinary for product: {}", id);
            }
        } catch (Exception e) {
            log.error("Failed to delete old image from Cloudinary for product: {}", id, e);
        }
    }
}
