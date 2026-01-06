package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.product.Product;
import com.coderrr1ck.backend.product.ProductNotFoundException;
import com.coderrr1ck.backend.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderMapper {
    @Autowired
    private ProductRepository productRepository;
    public Order toOrder(OrderRequest orderRequest,List<OrderItem> itemList, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setItems(itemList);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        return order;
    }

    public OrderResponse toOrderResponse(Order savedOrder) {
        OrderResponse response = new OrderResponse();
        response.setId(savedOrder.getOrderId());
        response.setStatus(savedOrder.getStatus());
        response.setOrderTotal(savedOrder.getTotalAmount());
        return response;
    }
}
