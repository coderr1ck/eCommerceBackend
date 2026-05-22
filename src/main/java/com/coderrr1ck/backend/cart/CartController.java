package com.coderrr1ck.backend.cart;

import com.coderrr1ck.backend.user.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        CartResponse cartResponse = cartService.getCartForUser(user);
        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("items")
    public ResponseEntity<Void> addCartItem(
            @Valid @RequestBody CartRequest cartRequest,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        cartService.addCartItem(user,cartRequest);
        return ResponseEntity.created(null).build();
    }

    @PutMapping("items/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
            @PathVariable("cartItemId") Integer itemId,
            @Valid @RequestBody CartItemRequest cartItemRequest,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        cartService.updateCartItem(user,itemId,cartItemRequest.getQuantity());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("items/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
            @PathVariable("cartItemId") Integer itemId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        cartService.deleteCartItem(user,itemId);
        return ResponseEntity.noContent().build();
    }



}
