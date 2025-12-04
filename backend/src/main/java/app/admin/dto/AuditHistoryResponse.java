package app.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response containing audit history for an entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditHistoryResponse {
    
    private String entityType;
    private String entityId;
    private int totalRevisions;
    private List<AuditRevision> revisions;
}
