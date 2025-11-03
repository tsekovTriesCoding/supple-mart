package app.cartitem.service;

import app.cartitem.dto.UpdateCartItemRequest;
import app.cartitem.model.CartItem;
import app.cartitem.repository.CartItemRepository;
import app.exception.BadRequestException;
import app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItem updateCartItemQuantity(UUID cartItemId, UUID userId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item with ID " + cartItemId + " not found"));

        if (cartItem.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + cartItem.getProduct().getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void deleteCartItem(UUID cartItemId, UUID userId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item with ID " + cartItemId + " not found"));

        cartItemRepository.delete(cartItem);
    }
}
