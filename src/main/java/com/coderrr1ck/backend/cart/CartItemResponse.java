package com.coderrr1ck.backend.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {
    private Integer itemId;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal itemTotal;
}
