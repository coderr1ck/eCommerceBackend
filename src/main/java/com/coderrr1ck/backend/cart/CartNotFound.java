package com.coderrr1ck.backend.cart;

public class CartNotFound extends RuntimeException {
    public CartNotFound(String s) {
        super(s);
    }
}
