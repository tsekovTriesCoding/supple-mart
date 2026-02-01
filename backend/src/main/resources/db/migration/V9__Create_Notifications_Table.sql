-- V9: Create notifications table for real-time in-app notifications

CREATE TABLE notifications (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    action_url VARCHAR(500),
    reference_id VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for efficient querying
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_read ON notifications(is_read);
CREATE INDEX idx_notification_user_read ON notifications(user_id, is_read);
