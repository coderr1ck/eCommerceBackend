package com.coderrr1ck.backend.productImage;

public class InvalidImageFile extends RuntimeException {
    public InvalidImageFile(String message) {
      super(message);
    }
}
