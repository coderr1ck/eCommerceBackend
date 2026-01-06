package com.coderrr1ck.backend.order;

public class ProductNotFoundInOrder extends RuntimeException {
    public ProductNotFoundInOrder(String message) {
        super(message);
    }
}
