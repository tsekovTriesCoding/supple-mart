package app.web.admin;

import app.admin.dto.AdminOrdersResponse;
import app.admin.dto.UpdateOrderStatusRequest;
import app.admin.service.AdminOrderService;
import app.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Orders", description = "Order management endpoints (requires ADMIN role)")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "Get all orders", description = "Retrieve paginated list of all orders with optional filters")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @GetMapping
    public ResponseEntity<AdminOrdersResponse> getAllOrders(
            @Parameter(description = "Filter by order status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter from date") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "Filter to date") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("Admin: Fetching all orders - page: {}, limit: {}", page, limit);
        AdminOrdersResponse response = adminOrderService.getAllOrders(status, startDate, endDate, page, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order status", description = "Update the status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        log.info("Admin: Updating order {} status to {}", orderId, request.getStatus());
        OrderResponse order = adminOrderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(order);
    }
}

