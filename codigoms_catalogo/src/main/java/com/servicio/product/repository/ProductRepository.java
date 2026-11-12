package com.servicio.product.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.servicio.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueAndStockQuantityGreaterThan(Integer quantity);
    List<Product> findByCategory_NameIgnoreCaseAndActiveTrue(String categoryName);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("select p from Product p where p.active = true " +
           "and (:minPrice is null or p.price >= :minPrice) " +
           "and (:maxPrice is null or p.price <= :maxPrice) " +
           "and (:categoryName is null or lower(p.category.name) = lower(:categoryName)) " +
           "and (:inStock is null or (:inStock = true and p.stockQuantity > 0) or (:inStock = false and p.stockQuantity = 0))")
    List<Product> searchActiveProducts(@Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice,
                                       @Param("categoryName") String categoryName,
                                       @Param("inStock") Boolean inStock);
}
