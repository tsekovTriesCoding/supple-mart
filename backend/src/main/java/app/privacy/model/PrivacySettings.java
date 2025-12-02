package app.privacy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "privacy_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "show_profile", nullable = false)
    private Boolean showProfile = true;

    @Column(name = "show_activity", nullable = false)
    private Boolean showActivity = false;

    @Column(name = "show_online_status", nullable = false)
    private Boolean showOnlineStatus = false;

    @Column(name = "share_analytics", nullable = false)
    private Boolean shareAnalytics = false;

    @Column(name = "share_marketing", nullable = false)
    private Boolean shareMarketing = false;

    @Column(name = "share_third_party", nullable = false)
    private Boolean shareThirdParty = false;

    @Column(name = "searchable", nullable = false)
    private Boolean searchable = true;

    @Column(name = "allow_messages", nullable = false)
    private Boolean allowMessages = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
