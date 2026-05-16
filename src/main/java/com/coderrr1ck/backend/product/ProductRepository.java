package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByNameAndActiveTrue(String name);
    Optional<Product> findByNameAndActiveFalse(String name);
    Optional<Product> findByNameAndActiveTrue(String name);


    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.primaryImage "+
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\' " +
            "AND p.active = true")
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(
            @Param("name") String name, Pageable pageable);


    @Query(value = "SELECT p FROM Product p " +
            "JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.primaryImage " +
            "WHERE p.category = :category " +
            "AND p.active = true",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE p.category = :category AND p.active = true")
    Page<Product> findByCategoryAndActiveTrue(Pageable pageRequest, @Param("category") Category category);


    @Query("SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.primaryImage WHERE p.active = true")
    Page<Product> findAllActiveProductsWithCategory(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndImages(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.availableStock = p.availableStock - :qty, p.reservedStock = p.reservedStock + :qty " +
            "WHERE p.productId = :id AND p.availableStock >= :qty AND p.active = true")
    int reserveStockAtomic(@Param("id") UUID id, @Param("qty") Integer qty);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.availableStock = p.availableStock + :qty, p.reservedStock = p.reservedStock - :qty " +
            "WHERE p.productId = :id AND p.reservedStock >= :qty AND p.active = true")
    int rollbackStockAtomic(@Param("id") UUID id, @Param("qty") Integer qty);


    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.reservedStock = p.reservedStock - :qty " +
            "WHERE p.productId = :id AND p.reservedStock >= :qty AND p.active = true")
    int deductStockAtomic(@Param("id") UUID id,@Param("qty") @Min(value = 0, message = "Order Quantity cannot be negative") Integer qty);
}
