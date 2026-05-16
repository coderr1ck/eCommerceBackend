package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.cart.Cart;
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
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(precision = 12, scale = 2,nullable = false)
    private BigDecimal priceAtPurchase;

    @Min(value = 0, message = "Order Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity;

    @Min(value = 0, message = "Order Item Total cannot be negative")
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
}
