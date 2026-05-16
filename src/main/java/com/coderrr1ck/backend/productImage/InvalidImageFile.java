package com.coderrr1ck.backend.product;

public class InvalidImageFile extends RuntimeException {
    public InvalidImageFile(String message) {
      super(message);
    }
}
