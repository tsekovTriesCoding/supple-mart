package app.privacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePrivacySettingsRequest {
    
    private Boolean showProfile;
    private Boolean showActivity;
    private Boolean showOnlineStatus;
    private Boolean shareAnalytics;
    private Boolean shareMarketing;
    private Boolean shareThirdParty;
    private Boolean searchable;
    private Boolean allowMessages;
}
