package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.product.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {

    @Id
    private String orderId;

    @NotNull
    @Indexed
    private String userId;

    @NotNull
    private List<OrderItem> items;

    @Min(value = 0, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    private OrderStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private boolean active = true;
}


