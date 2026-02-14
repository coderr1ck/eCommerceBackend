package com.coderrr1ck.backend.cart;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carts")
public class Cart {
    @Id
    private String cartId;
    private String userId;
    private CartItem[] items;
}
