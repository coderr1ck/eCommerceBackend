package com.coderrr1ck.backend.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {
    private UUID cartId;
    private Integer uniqueItems;
    private BigDecimal subTotal;
    private List<CartItemResponse> items;
}
