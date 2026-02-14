package com.coderrr1ck.backend.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private String id;
    private OrderStatus status;
    private BigDecimal orderTotal;
}
