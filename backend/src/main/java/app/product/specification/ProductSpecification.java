package app.product.specification;

import app.product.model.Category;
import app.product.model.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {
        // Utility class - prevent instantiation
    }

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

    public static Specification<Product> hasCategory(Category category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Product> fetchReviews() {
        return (root, query, cb) -> {
            // Only fetch for non-count queries
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("reviews", JoinType.LEFT);
            }
            return null;
        };
    }

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
