package app.cartitem.mapper;

import app.cartitem.dto.CartItemDTO;
import app.cartitem.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cartItem))")
    CartItemDTO toCartItemDTO(CartItem cartItem);

    default BigDecimal calculateSubtotal(CartItem cartItem) {
        if (cartItem == null || cartItem.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }
}
