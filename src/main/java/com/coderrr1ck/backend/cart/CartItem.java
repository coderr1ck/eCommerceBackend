package com.coderrr1ck.backend.cart;

import com.coderrr1ck.backend.order.OrderItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String cartItemId;
    private String productId;
    private int quantity;
    private Cart cartId;
}
