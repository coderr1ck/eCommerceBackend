package com.coderrr1ck.backend.productImage;

public class ProductImageNotFoundException extends RuntimeException {
    public ProductImageNotFoundException(String string) {
        super("Product Image Not found for Image Id :"+string);
    }
}
