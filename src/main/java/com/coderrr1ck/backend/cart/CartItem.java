package com.coderrr1ck.backend.cart;

import com.coderrr1ck.backend.order.OrderItem;
import com.coderrr1ck.backend.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ManyToOne
    @JoinColumn(name = "cart_id",nullable = false)
    @ToString.Exclude
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) // no need to mention inverse side in product as
    // we don't need to navigate from product to cart items
    @JoinColumn(name = "product_id",nullable = false)
    @ToString.Exclude
    private Product product;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2,nullable = false)
    private BigDecimal itemTotal;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    private boolean active = true;

}
