package app.admin.mapper;

import app.admin.dto.*;
import app.order.model.Order;
import app.order.model.OrderItem;
import app.product.model.Product;
import app.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    // Dashboard Stats
    default DashboardStatsDTO toDashboardStatsDTO(
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

    // Product Mapping Methods
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Product toProductEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "active", source = "request.isActive")
    void updateProductEntity(@MappingTarget Product product, UpdateProductRequest request);

    @Mapping(target = "inStock", expression = "java(product.getStockQuantity() > 0)")
    @Mapping(target = "isActive", source = "product.active")
    @Mapping(target = "totalSales", source = "totalSales")
    AdminProductDTO toAdminProductDTO(Product product, Integer totalSales);

    default List<AdminProductDTO> toAdminProductDTOList(List<Product> products, Map<UUID, Integer> salesMap) {
        return products.stream()
                .map(product -> toAdminProductDTO(product, salesMap.getOrDefault(product.getId(), 0)))
                .toList();
    }

    default AdminProductPageResponse toAdminProductPageResponse(Page<Product> productPage, Map<UUID, Integer> salesMap) {
        List<AdminProductDTO> productDTOs = toAdminProductDTOList(productPage.getContent(), salesMap);

        return AdminProductPageResponse.builder()
                .products(productDTOs)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .build();
    }

    // Order Mapping Methods
    @Mapping(target = "customerName", expression = "java(order.getUser().getFirstName() + \" \" + order.getUser().getLastName())")
    @Mapping(target = "customerEmail", source = "user.email")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "items", source = "items")
    AdminOrderDTO toAdminOrderDTO(Order order);

    @Mapping(target = "productName", source = "product.name")
    AdminOrderItemDTO toAdminOrderItemDTO(OrderItem orderItem);

    default AdminOrdersResponse toAdminOrdersResponse(Page<Order> orderPage) {
        if (orderPage == null) {
            return null;
        }

        List<AdminOrderDTO> orders = orderPage.getContent().stream()
                .map(this::toAdminOrderDTO)
                .toList();

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

    // User Mapping Methods
    AdminUserDTO toAdminUserDTO(User user);

    default AdminUsersResponse toAdminUsersResponse(Page<User> userPage) {
        if (userPage == null) {
            return null;
        }

        List<AdminUserDTO> users = userPage.getContent().stream()
                .map(this::toAdminUserDTO)
                .toList();

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
