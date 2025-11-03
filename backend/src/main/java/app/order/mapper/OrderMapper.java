package app.order.mapper;

import app.order.dto.OrderDTO;
import app.order.dto.OrderItemDTO;
import app.order.model.Order;
import app.order.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name().toLowerCase())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems().stream()
                        .map(this::toOrderItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .product(OrderItemDTO.ProductInfo.builder()
                        .id(orderItem.getProduct().getId())
                        .name(orderItem.getProduct().getName())
                        .imageUrl(orderItem.getProduct().getImageUrl())
                        .build())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}

