package app.product.service;

import app.notification.event.PriceDropEvent;
import app.notification.event.ProductRestockedEvent;
import app.product.model.Product;
import app.product.repository.ProductRepository;
import app.user.model.User;
import app.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Example helper class to demonstrate how to publish price drop and restock events
 * This can be integrated into your existing ProductService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductNotificationHelper {

    private final ApplicationEventPublisher eventPublisher;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    /**
     * Call this method when updating product price
     * It will automatically notify users who have the product in their wishlist
     */
    public void checkAndNotifyPriceDrop(UUID productId, Double oldPrice, Double newPrice) {
        if (newPrice >= oldPrice) {
            log.debug("Price did not drop for product: {}. Old: {}, New: {}", productId, oldPrice, newPrice);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Get all users who have this product in their wishlist
        List<User> interestedUsers = wishlistRepository.findUsersByProductId(productId);

        if (interestedUsers.isEmpty()) {
            log.debug("No users have product {} in wishlist. Skipping price drop notification.", productId);
            return;
        }

        log.info("Price dropped for product: {}. Notifying {} users", product.getName(), interestedUsers.size());
        
        // Convert users to notification data DTOs
        List<PriceDropEvent.UserNotificationData> userNotificationData = interestedUsers.stream()
                .map(user -> new PriceDropEvent.UserNotificationData(user.getId(), user.getEmail(), user.getFirstName()))
                .toList();

        // Publish event - NotificationEventListener will handle checking preferences and sending emails
        eventPublisher.publishEvent(new PriceDropEvent(this, product.getName(), oldPrice, newPrice, userNotificationData));

        log.info("PriceDropEvent published for product: {}", product.getName());
    }

    /**
     * Call this method when restocking a product
     * It will automatically notify users who have the product in their wishlist
     */
    public void checkAndNotifyRestock(UUID productId, Integer newStock) {
        if (newStock <= 0) {
            log.debug("Product {} is still out of stock. Stock: {}", productId, newStock);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Get all users who have this product in their wishlist
        List<User> interestedUsers = wishlistRepository.findUsersByProductId(productId);

        if (interestedUsers.isEmpty()) {
            log.debug("No users have product {} in wishlist. Skipping restock notification.", productId);
            return;
        }

        log.info("Product {} is back in stock. Notifying {} users", product.getName(), interestedUsers.size());
        
        // Convert users to notification data DTOs
        List<ProductRestockedEvent.UserNotificationData> userNotificationData = interestedUsers.stream()
                .map(user -> new ProductRestockedEvent.UserNotificationData(user.getId(), user.getEmail(), user.getFirstName()))
                .toList();

        // Publish event - NotificationEventListener will handle checking preferences and sending emails
        eventPublisher.publishEvent(new ProductRestockedEvent(this, product.getName(), userNotificationData));

        log.info("ProductRestockedEvent published for product: {}", product.getName());
    }
}
