package app.product.specification;

import app.product.model.Category;
import app.product.model.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * JPA Specifications for building dynamic, type-safe Product queries.
 * Each method returns a Specification that can be combined with others using .and() / .or()
 * 
 * Benefits over JPQL:
 * - Type-safe: compile-time checks instead of runtime errors
 * - Composable: combine specs like building blocks
 * - Readable: each filter is a separate, named method
 * - Maintainable: easy to add/remove filters without modifying query strings
 * - Testable: each specification can be unit tested in isolation
 */
public final class ProductSpecification {

    private ProductSpecification() {
        // Utility class - prevent instantiation
    }

    /**
     * Search by name or description (case-insensitive)
     */
    public static Specification<Product> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filter by category
     */
    public static Specification<Product> hasCategory(Category category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    /**
     * Filter by minimum price (inclusive)
     */
    public static Specification<Product> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /**
     * Filter by maximum price (inclusive)
     */
    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /**
     * Filter by active status
     */
    public static Specification<Product> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active);
    }

    /**
     * Fetch reviews eagerly (for product details)
     * Note: Only use for single product queries to avoid cartesian product issues
     */
    public static Specification<Product> fetchReviews() {
        return (root, query, cb) -> {
            // Only fetch for non-count queries
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("reviews", JoinType.LEFT);
            }
            return null;
        };
    }

    /**
     * Combine all product filters into a single specification.
     * Null parameters are automatically ignored.
     */
    public static Specification<Product> withFilters(
            String search,
            Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active) {
        return Specification.allOf(
                hasSearch(search),
                hasCategory(category),
                hasPriceGreaterThanOrEqual(minPrice),
                hasPriceLessThanOrEqual(maxPrice),
                isActive(active)
        );
    }
}
