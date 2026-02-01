package app.notification.dto;

import app.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private String referenceId;
}
