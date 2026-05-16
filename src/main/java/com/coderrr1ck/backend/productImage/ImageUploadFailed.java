package com.coderrr1ck.backend.productImage;

public class ImageUploadFailed extends RuntimeException{
    public ImageUploadFailed(String message) {
        super(message);
    }
}
