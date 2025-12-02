package app.privacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsResponse {
    private UUID id;
    private UUID userId;
    
    private Boolean showProfile;
    private Boolean showActivity;
    private Boolean showOnlineStatus;
    private Boolean shareAnalytics;
    private Boolean shareMarketing;
    private Boolean shareThirdParty;
    private Boolean searchable;
    private Boolean allowMessages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
