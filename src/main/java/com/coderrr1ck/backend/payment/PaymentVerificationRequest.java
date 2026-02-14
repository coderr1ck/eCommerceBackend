package com.coderrr1ck.backend.payment;

public record PaymentVerificationRequest (
            String orderId,
            String paymentId,
            String signature
){

}

