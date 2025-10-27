package app.cart.mapper;

import app.cart.dto.CartDTO;
import app.cart.dto.CartItemDTO;
import app.cart.model.Cart;
import app.cart.model.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {

    public CartDTO toCartDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItem> items = cart.getItems() != null ? cart.getItems() : new ArrayList<CartItem>();

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartDTO.builder()
                .id(cart.getId())
                .items(items.stream()
                        .map(this::toCartItemDTO)
                        .toList())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    public CartItemDTO toCartItemDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        BigDecimal subtotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productImageUrl(cartItem.getProduct().getImageUrl())
                .price(cartItem.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
