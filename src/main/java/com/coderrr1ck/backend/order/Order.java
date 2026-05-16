package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.address.Address;
import com.coderrr1ck.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
// apply unique constraint on orderId and userId to prevent duplicate orders for same user
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id") // apply not null constraint
    private Address address;

    @Min(value = 0, message = "Total items cannot be negative")
    @Column(nullable = false)
    private Integer totalItems;

    @Min(value = 0, message = "Total amount cannot be negative")
    @Column(precision = 12, scale = 2,nullable = false)
    private BigDecimal subTotal;

    @Column(unique = true,nullable = false)
    private UUID idempotencyKey;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderPaymentStatus orderPaymentStatus;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    private boolean active = true;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}


