package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.cart.CartItem;
import com.coderrr1ck.backend.product.Product;
import com.coderrr1ck.backend.product.ProductNotFoundException;
//import com.coderrr1ck.backend.product.ProductRepository;
import com.coderrr1ck.backend.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderMapper {

    public OrderResponse toOrderResponse(Order savedOrder) {
        OrderResponse response = new OrderResponse();
        response.setId(savedOrder.getOrderId());
        response.setStatus(savedOrder.getStatus());
        response.setPaymentStatus(savedOrder.getOrderPaymentStatus());
        response.setOrderTotal(savedOrder.getSubTotal());
        return response;
    }

    public OrderItem mapCartItemToOrderItem(CartItem cartItem,Order newOrder) {
        return OrderItem.builder()
                .product(cartItem.getProduct())
                .order(newOrder)
                .priceAtPurchase(cartItem.getProduct().getPrice())
                .quantity(cartItem.getQuantity())
                .itemTotal(cartItem.getItemTotal())
                .build();
    }
}
