package app.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Examples: Adding a product to wishlist that's already there,
 * creating a user with an email that already exists, etc.
 *
 * Results in HTTP 409 Conflict status code.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
