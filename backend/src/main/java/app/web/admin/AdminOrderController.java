package app.web.admin;

import app.admin.dto.AdminOrdersResponse;
import app.admin.dto.UpdateOrderStatusRequest;
import app.admin.service.AdminOrderService;
import app.order.dto.OrderResponse;
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
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<AdminOrdersResponse> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("Admin: Fetching all orders - page: {}, limit: {}", page, limit);
        AdminOrdersResponse response = adminOrderService.getAllOrders(status, startDate, endDate, page, limit);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        log.info("Admin: Updating order {} status to {}", orderId, request.getStatus());
        OrderResponse order = adminOrderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(order);
    }
}

