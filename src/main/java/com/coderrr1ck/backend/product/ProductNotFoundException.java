package com.coderrr1ck.backend.product;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("Product with id '" + id + "' not found.");
    }
}
