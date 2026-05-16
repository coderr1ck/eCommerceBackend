package com.coderrr1ck.backend.payment;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {
    @NotNull(message = "Order Id is required")
    private UUID orderId;

    @NotNull(message = "Payment Method is required")
    private PaymentMode paymentMode;
}
