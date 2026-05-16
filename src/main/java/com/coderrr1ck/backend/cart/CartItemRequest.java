package com.coderrr1ck.backend.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity ;
}
