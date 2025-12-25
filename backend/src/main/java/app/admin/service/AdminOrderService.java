package app.admin.service;

import app.admin.dto.AdminOrdersResponse;
import app.admin.mapper.AdminMapper;
import app.order.dto.OrderResponse;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderService orderService;
    private final AdminMapper adminMapper;

    public AdminOrdersResponse getAllOrders(String status, LocalDateTime startDate,
                                            LocalDateTime endDate, Integer page, Integer limit) {
        log.info("Admin: Fetching all orders - page: {}, limit: {}", page, limit);

        Page<Order> orderPage = orderService.getAllOrdersPage(status, startDate, endDate, page, limit);

        return adminMapper.toAdminOrdersResponse(orderPage);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, String statusStr) {
        log.info("Admin: Updating order {} status to {}", orderId, statusStr);

        OrderStatus newStatus = OrderStatus.valueOf(statusStr);

        return orderService.updateOrderStatus(orderId, newStatus);
    }
}

