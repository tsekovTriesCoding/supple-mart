package app.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|PAID|PROCESSING|SHIPPED|DELIVERED|CANCELLED",
            message = "Invalid order status. Valid statuses: PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED")
    private String status;
}

