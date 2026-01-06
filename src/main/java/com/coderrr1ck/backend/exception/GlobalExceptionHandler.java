package com.coderrr1ck.backend.exception;

import com.coderrr1ck.backend.auth.UserAlreadyExistsException;
import com.coderrr1ck.backend.category.CategoryAlreadyExistsException;
import com.coderrr1ck.backend.category.CategoryNotFoundException;
import com.coderrr1ck.backend.order.InsufficientStockException;
import com.coderrr1ck.backend.order.ProductNotFoundInOrder;
import com.coderrr1ck.backend.product.ProductAlreadyExistsException;
import com.coderrr1ck.backend.product.ProductNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CategoryAlreadyExistsException.class, ProductAlreadyExistsException.class, InsufficientStockException.class, UserAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleExceptionNotExist(Exception ex, HttpServletResponse res) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler({CategoryNotFoundException.class, ProductNotFoundException.class, ProductNotFoundInOrder.class})
    public ResponseEntity<ErrorResponse> handleExceptionNotFound(Exception ex, HttpServletResponse res) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayload(Exception ex) {
        ex.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request Payload.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletResponse res) throws IOException {
        ex.printStackTrace();


        if(ex instanceof AuthenticationException){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(
                            HttpStatus.UNAUTHORIZED,
                            ex.getMessage()
                    )
            );
        } else if (ex instanceof AccessDeniedException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse(
                            HttpStatus.FORBIDDEN,
                            ex.getMessage()
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went wrong "
                )
        );
    }







}
