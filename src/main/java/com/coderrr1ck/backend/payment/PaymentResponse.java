package com.coderrr1ck.backend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
}
