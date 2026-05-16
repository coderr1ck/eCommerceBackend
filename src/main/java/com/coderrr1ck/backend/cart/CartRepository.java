package com.coderrr1ck.backend.cart;

import com.coderrr1ck.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
    @Query("""
    SELECT DISTINCT c FROM Cart c
    LEFT JOIN FETCH c.cartItems ci
    LEFT JOIN FETCH ci.product p
    WHERE c.user = :user
    """)
    Optional<Cart> findCartWithItemsAndProductsByUserId(User user);
}
