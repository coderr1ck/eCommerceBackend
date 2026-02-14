package com.coderrr1ck.backend.payment;


import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "payments")
@Data
public abstract class Payment {

    @Id
    private String paymentId;

    private String orderId;

    private PaymentStatus paymentStatus;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private PaymentCurrency currency;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
