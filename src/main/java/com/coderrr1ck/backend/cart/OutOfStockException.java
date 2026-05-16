package com.coderrr1ck.backend.cart;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String s) {
        super(s);
    }
}
