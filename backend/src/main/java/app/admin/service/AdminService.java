package app.admin.service;

import app.admin.dto.CreateProductRequest;
import app.admin.dto.DashboardStatsDTO;
import app.admin.dto.ImageUploadResponse;
import app.admin.dto.UpdateProductRequest;
import app.admin.mapper.AdminProductMapper;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import app.order.repository.OrderRepository;
import app.product.dto.ProductDetailsDTO;
import app.product.dto.ProductPageResponse;
import app.product.mapper.ProductMapper;
import app.product.model.Category;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.user.repository.UserRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductMapper productMapper;
    private final AdminProductMapper adminProductMapper;

    private static final String UPLOAD_DIR = "uploads/products/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    public DashboardStatsDTO getDashboardStats() {
        log.info("Fetching dashboard statistics");

        Long totalProducts = productRepository.count();
        Long totalUsers = userRepository.count();
        Long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        Long pendingOrders = orderRepository.countPendingOrders();
        Long lowStockProducts = productRepository.countLowStockProducts();

        return DashboardStatsDTO.builder()
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .build();
    }

    public ProductPageResponse getAllProductsForAdmin(
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

        Page<Product> productPage = productRepository.findProductsWithFilters(
                search, category, minPrice, maxPrice, active, pageable
        );

        return productMapper.toPageResponse(productPage);
    }

    @Transactional
    public ProductDetailsDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        Product product = adminProductMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toDetailsDTO(savedProduct);
    }

    @Transactional
    public ProductDetailsDTO updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        adminProductMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully: {}", id);
        return productMapper.toDetailsDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        if (product.getOrderItems() != null && !product.getOrderItems().isEmpty()) {
            throw new BadRequestException("Cannot delete product with existing orders. Consider marking it as inactive instead.");
        }

        productRepository.delete(product);
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

