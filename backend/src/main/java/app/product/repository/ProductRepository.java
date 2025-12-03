package app.product.repository;

import app.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews r LEFT JOIN FETCH r.user WHERE p.id = :id")
    Optional<Product> findByIdWithReviews(@Param("id") UUID id);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity < 10")
    Long countLowStockProducts();
}
