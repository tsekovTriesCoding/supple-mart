package app.product.repository;

import app.product.model.Category;
import app.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.reviews WHERE " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:active IS NULL OR p.isActive = :active)")
    Page<Product> findProductsWithFilters(
            @Param("search") String search,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews r LEFT JOIN FETCH r.user WHERE p.id = :id")
    Optional<Product> findByIdWithReviews(@Param("id") UUID id);
}
