package app.web.admin;

import app.admin.dto.AuditHistoryResponse;
import app.admin.dto.AuditRevision;
import app.admin.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Audit", description = "Audit history management for admins")
public class AdminAuditController {

    private final AuditService auditService;

    @GetMapping("/recent")
    @Operation(
            summary = "Get recent audit activity",
            description = "Returns the most recent changes across all audited entities"
    )
    @ApiResponse(responseCode = "200", description = "Recent audit activity retrieved")
    public ResponseEntity<List<AuditRevision>> getRecentActivity(
            @Parameter(description = "Maximum number of entries to return")
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(auditService.getRecentAuditActivity(limit));
    }

    @GetMapping("/products/{productId}")
    @Operation(
            summary = "Get product audit history",
            description = "Returns all revisions for a specific product"
    )
    @ApiResponse(responseCode = "200", description = "Product audit history retrieved")
    public ResponseEntity<AuditHistoryResponse> getProductAuditHistory(
            @Parameter(description = "Product ID") @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(auditService.getProductAuditHistory(productId));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(
            summary = "Get order audit history",
            description = "Returns all revisions for a specific order"
    )
    @ApiResponse(responseCode = "200", description = "Order audit history retrieved")
    public ResponseEntity<AuditHistoryResponse> getOrderAuditHistory(
            @Parameter(description = "Order ID") @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(auditService.getOrderAuditHistory(orderId));
    }
}
