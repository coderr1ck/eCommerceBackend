package com.coderrr1ck.backend.order;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemRequest {
        @NotNull(message = "Product id required")
        @NotBlank(message = "Product id is required")
        @Pattern(regexp = "^[0-9a-fA-F]{24}$", message = "Please provide valid product id")
        private String productId;

        @Positive(message = "Quantity must be greater than 0")
        @NotNull(message = "Quantity is required , must be integer value.")
        private Integer quantity;
}
