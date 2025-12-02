package app.cart.mapper;

import app.cart.dto.CartResponse;
import app.cart.model.Cart;
import app.cartitem.dto.CartItemResponse;
import app.cartitem.mapper.CartItemMapper;
import app.cartitem.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public abstract class CartMapper {

    private CartItemMapper cartItemMapper;

    protected CartMapper() {
    }

    @Autowired
    public void setCartItemMapper(CartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    @Mapping(target = "items", expression = "java(mapCartItems(cart))")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(cart))")
    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    public abstract CartResponse toCartResponse(Cart cart);

    protected List<CartItemResponse> mapCartItems(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return new ArrayList<>();
        }
        return cart.getItems().stream()
                .map(cartItemMapper::toCartItemResponse)
                .toList();
    }

    protected BigDecimal calculateTotalAmount(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected Integer calculateTotalItems(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0;
        }
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
