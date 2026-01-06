package com.coderrr1ck.backend.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
//    private static class OrderItemResponse{
//        String id;
//        String name;
//        String quantity;
//        BigDecimal unitPrice;
//    }

    private String id;
    private OrderStatus status;
//    private List<OrderItemResponse> itemList;
    private BigDecimal orderTotal;
}
