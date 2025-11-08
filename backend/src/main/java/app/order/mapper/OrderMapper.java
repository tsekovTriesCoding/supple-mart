package app.order.mapper;

import app.order.dto.OrderDTO;
import app.order.dto.OrderItemDTO;
import app.order.model.Order;
import app.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name().toLowerCase())")
    OrderDTO toOrderDTO(Order order);

    @Mapping(target = "product", source = "product")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    OrderItemDTO.ProductInfo toProductInfo(app.product.model.Product product);
}

