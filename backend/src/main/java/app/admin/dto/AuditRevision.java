package app.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a single revision/audit entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRevision {
    
    private Long revisionNumber;
    private LocalDateTime revisionDate;
    private String revisionType; // ADD, MOD, DEL
    private String modifiedBy;
    private Object entityData;
}
