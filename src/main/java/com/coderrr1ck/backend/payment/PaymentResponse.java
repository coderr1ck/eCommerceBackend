package com.coderrr1ck.backend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String orderId;
    private PaymentStatus paymentStatus;
}
