package app.web.admin;

import app.admin.service.CacheManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCacheController {

    private final CacheManagementService cacheManagementService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, CacheManagementService.CacheStats>> getCacheStats() {
        return ResponseEntity.ok(cacheManagementService.getAllCacheStats());
    }

    @GetMapping("/names")
    public ResponseEntity<Iterable<String>> getCacheNames() {
        return ResponseEntity.ok(cacheManagementService.getCacheNames());
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        cacheManagementService.clearCache(cacheName);
        return ResponseEntity.ok(Map.of(
                "message", "Cache '" + cacheName + "' cleared successfully"
        ));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManagementService.clearAllCaches();
        return ResponseEntity.ok(Map.of(
                "message", "All caches cleared successfully"
        ));
    }
}
