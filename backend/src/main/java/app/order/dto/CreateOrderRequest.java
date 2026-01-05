package app.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    @NotNull(message = "Shipping cost is required")
    @PositiveOrZero(message = "Shipping cost must be zero or positive")
    private BigDecimal shippingCost;
    
    private String shippingMethod;
}

