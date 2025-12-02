package app.order.mapper;

import app.cartitem.model.CartItem;
import app.order.dto.OrderResponse;
import app.order.dto.OrderItemResponse;
import app.order.dto.OrderStats;
import app.order.dto.OrdersResponse;
import app.order.model.Order;
import app.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name().toLowerCase())")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "product", source = "product")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    OrderItemResponse.ProductInfo toProductInfo(app.product.model.Product product);

    /**
     * Converts a Page of Orders to OrdersResponse DTO
     */
    default OrdersResponse toOrdersResponse(Page<Order> orderPage) {
        List<OrderResponse> orders = orderPage.getContent().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        return OrdersResponse.builder()
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

    /**
     * Converts CartItem to OrderItem (without order reference)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", source = "product")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    OrderItem cartItemToOrderItem(CartItem cartItem);

    /**
     * Converts list of CartItems to OrderItems
     */
    default List<OrderItem> cartItemsToOrderItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::cartItemToOrderItem)
                .collect(Collectors.toList());
    }

    /**
     * Builds OrderStats from individual statistics
     */
    default OrderStats toOrderStats(Long totalOrders, Long pendingCount, Long paidCount,
                                          Long processingCount, Long shippedCount, Long deliveredCount,
                                          Long cancelledCount, java.math.BigDecimal totalSpent) {
        return OrderStats.builder()
                .totalOrders(totalOrders)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .processingCount(processingCount)
                .shippedCount(shippedCount)
                .deliveredCount(deliveredCount)
                .cancelledCount(cancelledCount)
                .totalSpent(totalSpent)
                .build();
    }

    /**
     * Creates Order entity from user, order number, total amount, shipping address, and cart items
     */
    default Order toOrder(app.user.model.User user, String orderNumber, java.math.BigDecimal totalAmount,
                          String shippingAddress, List<CartItem> cartItems) {
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .status(app.order.model.OrderStatus.PENDING)
                .shippingAddress(shippingAddress)
                .build();

        List<OrderItem> orderItems = cartItemsToOrderItems(cartItems);
        orderItems.forEach(orderItem -> orderItem.setOrder(order));
        order.setItems(orderItems);

        return order;
    }
}

