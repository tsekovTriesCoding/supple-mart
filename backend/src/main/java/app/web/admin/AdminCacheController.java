package app.web.admin;

import app.admin.service.CacheManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Cache", description = "Cache management endpoints (requires ADMIN role)")
public class AdminCacheController {

    private final CacheManagementService cacheManagementService;

    @Operation(summary = "Get cache statistics", description = "Retrieve statistics for all caches")
    @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, CacheManagementService.CacheStats>> getCacheStats() {
        return ResponseEntity.ok(cacheManagementService.getAllCacheStats());
    }

    @Operation(summary = "Get cache names", description = "List all available cache names")
    @ApiResponse(responseCode = "200", description = "Cache names retrieved successfully")
    @GetMapping("/names")
    public ResponseEntity<Iterable<String>> getCacheNames() {
        return ResponseEntity.ok(cacheManagementService.getCacheNames());
    }

    @Operation(summary = "Clear specific cache", description = "Clear a cache by name")
    @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        cacheManagementService.clearCache(cacheName);
        return ResponseEntity.ok(Map.of(
                "message", "Cache '" + cacheName + "' cleared successfully"
        ));
    }

    @Operation(summary = "Clear all caches", description = "Clear all application caches")
    @ApiResponse(responseCode = "200", description = "All caches cleared successfully")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManagementService.clearAllCaches();
        return ResponseEntity.ok(Map.of(
                "message", "All caches cleared successfully"
        ));
    }
}
