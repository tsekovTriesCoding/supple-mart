export interface PrivacySettings {
  id: string;
  userId: string;

  // Profile Visibility
  showProfile: boolean;
  showActivity: boolean;
  showOnlineStatus: boolean;

  // Data Sharing
  shareAnalytics: boolean;
  shareMarketing: boolean;
  shareThirdParty: boolean;

  // Communication
  searchable: boolean;
  allowMessages: boolean;

  createdAt: string;
  updatedAt: string;
}

export interface UpdatePrivacySettingsRequest {
  showProfile?: boolean;
  showActivity?: boolean;
  showOnlineStatus?: boolean;
  shareAnalytics?: boolean;
  shareMarketing?: boolean;
  shareThirdParty?: boolean;
  searchable?: boolean;
  allowMessages?: boolean;
}
