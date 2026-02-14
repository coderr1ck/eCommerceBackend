package com.coderrr1ck.backend.payment;

public class PaymentAlreadyCompleted extends RuntimeException {
    public PaymentAlreadyCompleted(String paymentId) {
        super("Payment with ID " + paymentId + " already done.");
    }
}
