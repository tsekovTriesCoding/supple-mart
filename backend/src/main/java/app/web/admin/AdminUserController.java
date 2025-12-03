package app.web.admin;

import app.admin.dto.AdminUsersResponse;
import app.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users", description = "User management endpoints (requires ADMIN role)")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get all users", description = "Retrieve paginated list of all users with optional filters")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<AdminUsersResponse> getAllUsers(
            @Parameter(description = "Search by name or email") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Admin: Fetching all users - page: {}, size: {}, role: {}", page, size, role);
        AdminUsersResponse response = adminUserService.getAllUsers(search, role, page, size);
        return ResponseEntity.ok(response);
    }
}

