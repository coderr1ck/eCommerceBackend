package com.coderrr1ck.backend.cart;

public class CartItemNotFound extends RuntimeException{
    public CartItemNotFound(String s) {
        super(s);
    }
}
