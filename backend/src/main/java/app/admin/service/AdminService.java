package app.admin.service;

import app.admin.dto.*;
import app.admin.mapper.AdminProductMapper;
import app.exception.BadRequestException;
import app.product.dto.ProductDetailsDTO;
import app.product.mapper.ProductMapper;
import app.product.model.Category;
import app.product.model.Product;
import app.product.service.ProductService;
import app.order.service.OrderService;
import app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductMapper productMapper;
    private final AdminProductMapper adminProductMapper;

    private static final String UPLOAD_DIR = "uploads/products/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    public DashboardStatsDTO getDashboardStats() {
        log.info("Fetching dashboard statistics");

        Long totalProducts = productService.getTotalProductsCount();
        Long totalUsers = userService.getTotalUsersCount();
        Long totalOrders = orderService.getTotalOrdersCount();
        BigDecimal totalRevenue = orderService.getTotalRevenue();
        Long pendingOrders = orderService.getPendingOrdersCount();
        Long lowStockProducts = productService.getLowStockProductsCount();

        return DashboardStatsDTO.builder()
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .build();
    }

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

        // Calculate total sales for each product
        Map<UUID, Integer> salesMap = new HashMap<>();
        for (Product product : productPage.getContent()) {
            Integer totalSales = orderService.getTotalSalesByProductId(product.getId());
            salesMap.put(product.getId(), totalSales);
        }

        return adminProductMapper.toAdminPageResponse(productPage, salesMap);
    }

    @Transactional
    public ProductDetailsDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        Product product = adminProductMapper.toEntity(request);
        Product savedProduct = productService.createProduct(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toDetailsDTO(savedProduct);
    }

    @Transactional
    public ProductDetailsDTO updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productService.getProductById(id);

        adminProductMapper.updateEntity(product, request);
        Product updatedProduct = productService.updateProduct(product);

        log.info("Product updated successfully: {}", id);
        return productMapper.toDetailsDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);
        productService.deleteProduct(id);
        log.info("Product deleted successfully: {}", id);
    }

    @Transactional
    public ImageUploadResponse uploadProductImage(MultipartFile file) {
        log.info("Uploading product image: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new BadRequestException("Invalid file type. Allowed types: jpg, jpeg, png, gif, webp");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/products/" + uniqueFilename;
            log.info("Image uploaded successfully: {}", imageUrl);

            return ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Image uploaded successfully")
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    private boolean hasValidExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}

