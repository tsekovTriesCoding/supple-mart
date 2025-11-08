package app.cartitem.mapper;

import app.cartitem.dto.CartItemDTO;
import app.cartitem.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl")
    CartItemDTO toCartItemDTO(CartItem cartItem);
}
