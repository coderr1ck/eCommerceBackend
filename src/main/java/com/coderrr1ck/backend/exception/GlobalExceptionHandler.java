package com.coderrr1ck.backend.exception;

import com.coderrr1ck.backend.address.AddressNotFound;
import com.coderrr1ck.backend.auth.UserAlreadyExistsException;
import com.coderrr1ck.backend.cart.CartItemNotFound;
import com.coderrr1ck.backend.cart.CartNotFound;
import com.coderrr1ck.backend.cart.ItemAlreadyExistsInCartException;
import com.coderrr1ck.backend.cart.OutOfStockException;
import com.coderrr1ck.backend.category.CategoryAlreadyExistsException;
import com.coderrr1ck.backend.category.CategoryNotFoundException;
import com.coderrr1ck.backend.order.InsufficientStockException;
import com.coderrr1ck.backend.order.InvalidOrderRequestException;
import com.coderrr1ck.backend.order.OrderNotFoundException;
import com.coderrr1ck.backend.payment.InvalidPaymentRequest;
import com.coderrr1ck.backend.payment.PaymentAlreadyCompleted;
import com.coderrr1ck.backend.productImage.ImageUploadFailed;
import com.coderrr1ck.backend.productImage.ImagesAlreadyUploded;
import com.coderrr1ck.backend.productImage.InvalidImageFile;
import com.coderrr1ck.backend.product.ProductAlreadyExistsException;
import com.coderrr1ck.backend.product.ProductNotFoundException;
import com.coderrr1ck.backend.productImage.ProductImageNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ImagesAlreadyUploded.class,InvalidImageFile.class,CategoryAlreadyExistsException.class, ProductAlreadyExistsException.class, InsufficientStockException.class, UserAlreadyExistsException.class, PaymentAlreadyCompleted.class, InvalidPaymentRequest.class})
    public ResponseEntity<ErrorResponse> handleExceptionNotExist(Exception ex, HttpServletResponse res) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler({AddressNotFound.class,CartItemNotFound.class, CartNotFound.class,ProductImageNotFoundException.class,CategoryNotFoundException.class, ProductNotFoundException.class, OrderNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleExceptionNotFound(Exception ex, HttpServletResponse res) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler({OutOfStockException.class,ItemAlreadyExistsInCartException.class,ImageUploadFailed.class, InvalidOrderRequestException.class})
    public ResponseEntity<ErrorResponse> handleExceptionBadRequest(Exception ex, HttpServletResponse res) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

//    new class check it's actual working and usage , look up it's usage then use it
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<Map<String, String>> handleParameterValidation(ConstraintViolationException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getConstraintViolations().forEach(violation -> {
//            // Extracts the field name from the property path
//            String propertyPath = violation.getPropertyPath().toString();
//            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
//            errors.put(fieldName, violation.getMessage());
//        });
//        return ResponseEntity.badRequest().body(errors);
//    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName(); // The parameter name (e.g., "id")
        String type = ex.getRequiredType().getSimpleName(); // The expected type (e.g., "UUID")
        Object value = ex.getValue(); // The "bad" value sent by user

        String message = String.format("Parameter '%s' should be of type '%s'. Got: '%s'", name, type, value);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({MissingServletRequestPartException.class,HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleInvalidPayload(Exception ex) {
        ex.printStackTrace();
        if(ex instanceof HttpMessageNotReadableException){
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Malformed Request Payload ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }else{
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
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
