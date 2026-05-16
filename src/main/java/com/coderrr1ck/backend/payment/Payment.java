package com.coderrr1ck.backend.payment;


import com.coderrr1ck.backend.order.Order;
import com.coderrr1ck.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.dialect.OracleDialect;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Column(unique = true)
    private String gatewayOrderRefId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentCurrency currency;

    @Enumerated(EnumType.STRING)
    private PaymentGateway paymentGateway;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PaymentAttempt> attemptsList = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    private boolean active = true;

}
