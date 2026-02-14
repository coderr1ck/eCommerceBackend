package com.coderrr1ck.backend.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {
    @NotBlank(message = "Payment ID is required")
    @Pattern(regexp = "^[0-9a-fA-F]{24}$", message = "Please provide valid Payment ID")
    private String paymentId;

    @NotBlank(message = "Order ID is required")
    @Pattern(regexp = "^[0-9a-fA-F]{24}$", message = "Please provide valid Order ID")
    private String orderId;

    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    private boolean onlinePayment;
}
