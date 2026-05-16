package com.coderrr1ck.backend.product;

public class ImageUploadFailed extends RuntimeException{
    public ImageUploadFailed(String message) {
        super(message);
    }
}
