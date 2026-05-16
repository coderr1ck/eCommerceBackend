package com.coderrr1ck.backend.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderResponse {
    private UUID id;
    private OrderStatus status;
    private OrderPaymentStatus paymentStatus;
    private BigDecimal orderTotal;
}
