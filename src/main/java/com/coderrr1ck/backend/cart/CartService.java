package com.coderrr1ck.backend.cart;

import com.coderrr1ck.backend.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartMapper mapper;
    private final CartRepository cartRepository;


    public void addCartItem(User user, CartRequest cartRequest) {
        if(user == null || !user.isActive()) throw new UsernameNotFoundException("User not found :"+user);

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .items(0)
                                .subTotal(BigDecimal.ZERO)
                                .cartItems(new ArrayList<>())
                                .build()
                ));

        CartItem newCartItem = mapper.mapCartRequestToCartItem(cartRequest);

        Optional<CartItem> existing = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getProductId()
                        .equals(newCartItem.getProduct().getProductId()))
                .findFirst();


        if (existing.isPresent()) {
            CartItem existingCartItem = existing.get();
            if(existingCartItem.isActive()){
                log.info("Item with product id {} already exists in cart for user {}", existingCartItem.getProduct().getProductId(), user.getEmail());
                throw new ItemAlreadyExistsInCartException("Item already exists in cart .");
            }else{
                existingCartItem.setActive(true);
                existingCartItem.setQuantity(newCartItem.getQuantity());
                existingCartItem.setItemTotal(newCartItem.getItemTotal());
            }
        } else {
            cart.addCartItem(newCartItem);
        }

        Cart updatedCart = recalculateCart(cart);
        cartRepository.save(updatedCart);
    }

    public void updateCartItem(User user, Integer itemId, Integer quantity) {
        Cart cart = getUserCartFromDB(user);
        CartItem savedItem = getSavedCartItemByIdFromCart(cart,itemId);

        if( quantity > savedItem.getProduct().getAvailableStock()){
            log.info("Requested quantity {} exceeds available stock {} for product {}",
                    quantity, savedItem.getProduct().getAvailableStock(), savedItem.getProduct().getName());
            throw new OutOfStockException("Requested quantity exceeds available stock for product ");
        }

        savedItem.setQuantity(quantity);
        savedItem.setItemTotal(savedItem.getProduct().getPrice().multiply(BigDecimal.valueOf(quantity)));

        Cart updatedCart = recalculateCart(cart);
        cartRepository.save(updatedCart);
    }

    public void deleteCartItem(User user, Integer itemId) {
        Cart cart = getUserCartFromDB(user);
        CartItem savedItem = getSavedCartItemByIdFromCart(cart,itemId);

        savedItem.setActive(false);
        savedItem.setItemTotal(BigDecimal.ZERO);
        savedItem.setQuantity(0);

        Cart updatedCart = recalculateCart(cart);
        cartRepository.save(updatedCart);
    }

    public Cart recalculateCart(Cart cart){
        int totalItems = (int) cart.getCartItems().stream()
                .filter(CartItem::isActive)
                .count();

        BigDecimal subTotal = cart.getCartItems().stream()
                .filter(CartItem::isActive)
                .map(CartItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setItems(totalItems);
        cart.setSubTotal(subTotal);
        return cart;
    }

    public CartResponse getCartForUser(User user) {
        Cart cart = getUserCartWithItemsAndProductFromDB(user);
        return mapper.mapCartToCartResponse(cart);
    }

    public Cart getUserCartFromDB(User user){
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFound("Cart doesn't exist for user :"+user.getEmail()));
    }

    public Cart getUserCartWithItemsAndProductFromDB(User user){
        return cartRepository.findCartWithItemsAndProductsByUserId(user)
                .orElseThrow(()-> new CartNotFound("Cart doesn't exist for user :"+user.getEmail()));
    }

    public CartItem getSavedCartItemByIdFromCart(Cart cart,Integer itemId){
        List<CartItem> items = cart.getCartItems();
        return items.stream()
                .filter(item -> item.getItemId().equals(itemId) && item.isActive())
                .findFirst()
                .orElseThrow(()->new CartItemNotFound("Cart Item not found with Id :"+itemId));

    }

    public void clearCart(Cart cart) {
        cart.getCartItems().clear();
        Cart updatedCart = recalculateCart(cart);
        cartRepository.save(updatedCart);
    }
}
