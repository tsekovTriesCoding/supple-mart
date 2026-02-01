package app.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {
    private List<NotificationDto> notifications;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private long unreadCount;
    private Map<String, Long> unreadByType;
}
