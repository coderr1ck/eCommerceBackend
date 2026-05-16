package com.coderrr1ck.backend.address;

public class InvalidAddressRequest extends RuntimeException {
    public InvalidAddressRequest(String message) {
        super(message);
    }
}
