package app.admin.mapper;

import app.admin.dto.*;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.product.model.Product;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AdminMapper {

    public DashboardStatsDTO toDashboardStatsDTO(
            Long totalProducts,
            Long totalUsers,
            Long totalOrders,
            BigDecimal totalRevenue,
            Long pendingOrders,
            Long lowStockProducts
    ) {
        return DashboardStatsDTO.builder()
                .totalProducts(totalProducts)
                .totalCustomers(totalUsers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .build();
    }

    //Product Mapping Methods

    public Product toProductEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity())
                .isActive(request.getIsActive())
                .build();
    }

    public void updateProductEntity(Product product, UpdateProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getIsActive());
    }

    public AdminProductDTO toAdminProductDTO(Product product, Integer totalSales) {
        if (product == null) {
            return null;
        }

        return AdminProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .inStock(product.getStockQuantity() > 0)
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .totalSales(totalSales != null ? totalSales : 0)
                .build();
    }

    public List<AdminProductDTO> toAdminProductDTOList(List<Product> products, Map<UUID, Integer> salesMap) {
        return products.stream()
                .map(product -> toAdminProductDTO(product, salesMap.getOrDefault(product.getId(), 0)))
                .collect(Collectors.toList());
    }

    public AdminProductPageResponse toAdminProductPageResponse(Page<Product> productPage, Map<UUID, Integer> salesMap) {
        List<AdminProductDTO> productDTOs = toAdminProductDTOList(productPage.getContent(), salesMap);

        return AdminProductPageResponse.builder()
                .products(productDTOs)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .build();
    }

    //Order Mapping Methods

    public AdminOrderDTO toAdminOrderDTO(Order order) {
        if (order == null) {
            return null;
        }

        String customerName = order.getUser().getFirstName() + " " + order.getUser().getLastName();

        List<AdminOrderItemDTO> items = order.getItems() != null
                ? order.getItems().stream()
                    .map(this::toAdminOrderItemDTO)
                    .collect(Collectors.toList())
                : List.of();

        return AdminOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(customerName)
                .customerEmail(order.getUser().getEmail())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private AdminOrderItemDTO toAdminOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return AdminOrderItemDTO.builder()
                .id(orderItem.getId())
                .productName(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }

    public AdminOrdersResponse toAdminOrdersResponse(Page<Order> orderPage) {
        if (orderPage == null) {
            return null;
        }

        List<AdminOrderDTO> orders = orderPage.getContent().stream()
                .map(this::toAdminOrderDTO)
                .collect(Collectors.toList());

        return AdminOrdersResponse.builder()
                .orders(orders)
                .currentPage(orderPage.getNumber())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .size(orderPage.getSize())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    //User Mapping Methods

    public AdminUserDTO toAdminUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return AdminUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public AdminUsersResponse toAdminUsersResponse(Page<User> userPage) {
        if (userPage == null) {
            return null;
        }

        List<AdminUserDTO> users = userPage.getContent().stream()
                .map(this::toAdminUserDTO)
                .collect(Collectors.toList());

        return AdminUsersResponse.builder()
                .content(users)
                .number(userPage.getNumber())
                .size(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }
}
