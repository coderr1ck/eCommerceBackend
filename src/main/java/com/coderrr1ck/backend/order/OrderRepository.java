package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByActiveTrue(Pageable pageRequest);
    Page<Order> findByUserAndActiveTrue(Pageable pageRequest, User user);

    Optional<Order> findByIdempotencyKey(UUID idempotencyKey);

    @Query(
        """
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product p
        WHERE o.orderId = :orderId
        """
    )
    Optional<Order> fetchByIdWithItemsAndProducts(UUID orderId);

}
