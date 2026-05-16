package com.coderrr1ck.backend.cart;

public class ItemAlreadyExistsInCartException extends RuntimeException {
    public ItemAlreadyExistsInCartException(String s) {
        super(s);
    }
}
