package com.coderrr1ck.backend.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemResponse {
        UUID id;
        String product;
        Integer quantity;
        BigDecimal itemTotal;
}
