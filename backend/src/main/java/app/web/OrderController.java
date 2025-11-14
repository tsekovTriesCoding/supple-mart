package app.web;

import app.order.dto.CreateOrderRequest;
import app.order.dto.OrderDTO;
import app.order.dto.OrderStatsDTO;
import app.order.dto.OrdersResponse;
import app.order.service.OrderService;
import app.security.CustomUserDetails;
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
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<OrdersResponse> getUserOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {

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

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsDTO> getOrderStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderStatsDTO stats = orderService.getUserOrderStats(userDetails.getId());
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderDTO order = orderService.createOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDTO order = orderService.cancelOrder(orderId, userDetails.getId());
        return ResponseEntity.ok(order);
    }
}
