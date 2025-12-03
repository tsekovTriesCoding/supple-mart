package app.web;

import app.order.dto.CreateOrderRequest;
import app.order.dto.OrderResponse;
import app.order.dto.OrderStats;
import app.order.dto.OrdersResponse;
import app.order.service.OrderService;
import app.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Get user orders", description = "Retrieve paginated list of user's orders with optional filters")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @GetMapping
    public ResponseEntity<OrdersResponse> getUserOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter orders from this date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Filter orders until this date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Items per page") @RequestParam(required = false) Integer limit) {

        OrdersResponse response = orderService.getUserOrders(
                userDetails.getId(),
                status,
                startDate,
                endDate,
                page,
                limit
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order statistics", description = "Retrieve order statistics for the current user")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = OrderStats.class)))
    @GetMapping("/stats")
    public ResponseEntity<OrderStats> getOrderStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderStats stats = orderService.getUserOrderStats(userDetails.getId());
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Create order", description = "Create a new order from cart items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or empty cart")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse order = orderService.createOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Operation(summary = "Cancel order", description = "Cancel a pending order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderResponse order = orderService.cancelOrder(orderId, userDetails.getId());
        return ResponseEntity.ok(order);
    }
}
