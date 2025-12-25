-- V11: Create notification_preferences table

CREATE TABLE IF NOT EXISTS notification_preferences (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    order_updates BOOLEAN NOT NULL DEFAULT TRUE,
    shipping_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    promotional_emails BOOLEAN NOT NULL DEFAULT TRUE,
    newsletter BOOLEAN NOT NULL DEFAULT TRUE,
    product_recommendations BOOLEAN NOT NULL DEFAULT TRUE,
    price_drop_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    back_in_stock_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    account_security_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    password_reset_emails BOOLEAN NOT NULL DEFAULT TRUE,
    review_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

