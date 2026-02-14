package com.coderrr1ck.backend.payment;

public class InvalidPaymentRequest extends RuntimeException {
    public InvalidPaymentRequest(String message) {
        super(message);
    }
}
