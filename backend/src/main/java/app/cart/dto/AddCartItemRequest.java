package app.cart.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddCartItemRequest {
    private UUID productId;
    private Integer quantity;
}
