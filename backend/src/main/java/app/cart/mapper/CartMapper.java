package app.cart.mapper;

import app.cart.dto.CartDTO;
import app.cart.model.Cart;
import app.cart.item.dto.CartItemDTO;
import app.cart.item.mapper.CartItemMapper;
import app.cart.item.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final CartItemMapper cartItemMapper;

    public CartDTO toCartDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        // Handle null items list
        var items = cart.getItems() != null ? cart.getItems() : new ArrayList<CartItem>();

        var cartItemDTOs = items.stream()
                .map(cartItemMapper::toCartItemDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = cartItemDTOs.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = cartItemDTOs.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();

        return CartDTO.builder()
                .id(cart.getId())
                .items(cartItemDTOs)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }
}
