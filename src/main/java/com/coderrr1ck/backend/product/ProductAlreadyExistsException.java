package com.coderrr1ck.backend.product;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String name) {
        super("Product with name '" + name + "' already exists.");
    }
}
