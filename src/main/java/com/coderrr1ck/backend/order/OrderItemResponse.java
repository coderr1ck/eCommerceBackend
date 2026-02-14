package com.coderrr1ck.backend.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemResponse {
        String id;
        String product;
        Integer quantity;
        BigDecimal itemTotal;
}
