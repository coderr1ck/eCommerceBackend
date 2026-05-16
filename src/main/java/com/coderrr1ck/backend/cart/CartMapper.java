package com.coderrr1ck.backend.cart;

import com.coderrr1ck.backend.product.Product;
import com.coderrr1ck.backend.product.ProductNotFoundException;
import com.coderrr1ck.backend.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CartMapper {

    private final ProductRepository productRepository;

    public CartItem mapCartRequestToCartItem(CartRequest cartRequest) {
        Product product = productRepository.findById(cartRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(cartRequest.getProductId().toString()));

        return  CartItem.builder()
                .product(product)
                .quantity(1)
                .itemTotal(product.getPrice())
                .active(true)
                .build();
    }

    public CartResponse mapCartToCartResponse(Cart cart) {
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .uniqueItems(cart.getItems())
                .subTotal(cart.getSubTotal())
                .items(cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.isActive())
                        .map(cartItem -> CartItemResponse.builder()
                                .itemId(cartItem.getItemId())
                        .productId(cartItem.getProduct().getProductId())
                        .productName(cartItem.getProduct().getName())
                        .quantity(cartItem.getQuantity())
                        .itemTotal(cartItem.getItemTotal())
                        .build())
                        .toList())
                .build();
    }
}
