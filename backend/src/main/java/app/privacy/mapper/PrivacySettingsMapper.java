package app.privacy.mapper;

import app.privacy.dto.PrivacySettingsResponse;
import app.privacy.dto.UpdatePrivacySettingsRequest;
import app.privacy.model.PrivacySettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PrivacySettingsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "showProfile", constant = "true")
    @Mapping(target = "showActivity", constant = "false")
    @Mapping(target = "showOnlineStatus", constant = "false")
    @Mapping(target = "shareAnalytics", constant = "false")
    @Mapping(target = "shareMarketing", constant = "false")
    @Mapping(target = "shareThirdParty", constant = "false")
    @Mapping(target = "searchable", constant = "true")
    @Mapping(target = "allowMessages", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract PrivacySettings createDefaultSettings(UUID userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateSettingsFromRequest(@MappingTarget PrivacySettings settings,
                                                    UpdatePrivacySettingsRequest request);

    public abstract PrivacySettingsResponse toResponse(PrivacySettings settings);
}
