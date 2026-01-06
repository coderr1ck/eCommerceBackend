package com.coderrr1ck.backend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private Instant timestamp;
    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.status = httpStatus.value();
        this.message = message;
        this.timestamp = Instant.now();
    }
}
