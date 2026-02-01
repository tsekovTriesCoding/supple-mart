package app.notification.model;

public enum NotificationType {
    // Order related
    ORDER_PLACED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    
    // Product related
    PRICE_DROP,
    BACK_IN_STOCK,
    LOW_STOCK,
    
    // Account related
    ACCOUNT_SECURITY,
    PASSWORD_CHANGED,
    PROFILE_UPDATED,
    
    // Marketing
    PROMOTIONAL,
    NEWSLETTER,
    PRODUCT_RECOMMENDATION,
    
    // Review related
    REVIEW_REMINDER,
    REVIEW_RESPONSE,
    
    // System
    SYSTEM_ANNOUNCEMENT,
    WELCOME
}
