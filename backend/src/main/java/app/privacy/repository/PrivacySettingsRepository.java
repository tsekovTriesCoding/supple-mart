package app.privacy.repository;

import app.privacy.model.PrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivacySettingsRepository extends JpaRepository<PrivacySettings, UUID> {
    
    Optional<PrivacySettings> findByUserId(UUID userId);
    
    boolean existsByUserId(UUID userId);
}
