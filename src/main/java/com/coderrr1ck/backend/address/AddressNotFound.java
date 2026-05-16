package com.coderrr1ck.backend.address;

public class AddressNotFound extends RuntimeException {
    public AddressNotFound(String message) {
        super(message);
    }
}
