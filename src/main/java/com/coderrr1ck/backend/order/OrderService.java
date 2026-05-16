package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.cart.Cart;
import com.coderrr1ck.backend.cart.CartItem;
import com.coderrr1ck.backend.cart.CartService;
import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import com.coderrr1ck.backend.payment.InvalidPaymentRequest;
import com.coderrr1ck.backend.product.ProductRepository;
import com.coderrr1ck.backend.user.User;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final CartService cartService;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse placeOrder(User user,OrderRequest orderRequest) {

        Optional<Order> savedOrderDB = orderRepository.findByIdempotencyKey(orderRequest.getIdempotencyKey());
        if(savedOrderDB.isPresent()){
            Order savedOrder = savedOrderDB.get();
            if(!savedOrder.isActive()){
                log.info("Order with the same idempotency key "+savedOrder.getIdempotencyKey()+"already exists but is inactive.");
                throw new InvalidOrderRequestException("Unable to place this order now . Please try again later");
            }

            if(!savedOrder.getStatus().equals(OrderStatus.PLACED)){
                log.info("Cannot place order with id :"+savedOrder.getOrderId()+" status : "+savedOrder.getStatus());
                throw new InvalidOrderRequestException("Unable to place this order now . Please try again later");
            }

            return mapper.toOrderResponse(savedOrder);
        }

        Cart cart = cartService.getUserCartWithItemsAndProductFromDB(user);
        List<CartItem> cartItems = cart.getCartItems();

        if(cartItems == null || cartItems.isEmpty()){
            throw new InvalidOrderRequestException("Cart is empty. Cannot place order.");
        }

        Order newOrder = Order.builder()
                .user(user)
                .orderItems(new ArrayList<>())
                .address(null)
                .totalItems(cart.getItems())
                .subTotal(cart.getSubTotal())
                .idempotencyKey(orderRequest.getIdempotencyKey())
                .status(OrderStatus.PLACED)
                .orderPaymentStatus(OrderPaymentStatus.PAYMENT_PENDING)
                .active(true)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem item : cartItems){
            int rowsAffected = productRepository.reserveStockAtomic(item.getProduct().getProductId(), item.getQuantity());
            if(rowsAffected == 0){
                throw new InsufficientStockException("Product currently out of stock "+item.getProduct().getName());
            }
            orderItems.add(mapper.mapCartItemToOrderItem(item,newOrder));
        }

        newOrder.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(newOrder);
        if(savedOrder != null){
            cartService.clearCart(cart);
        }
        return mapper.toOrderResponse(savedOrder);
    }




    public PagedResponseDTO<OrderResponse> getAllOrders(SearchRequest searchRequest) {
        Pageable pageRequest = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Order> pagedResponse = pagedResponse = orderRepository.findByActiveTrue(pageRequest);
        if(!StringUtils.isBlank(searchRequest.getQuery())){
            //immplement search feature later
        }
        List<Order> orders = pagedResponse.getContent();
        List<OrderResponse> responseList = orders.stream()
                .map(mapper::toOrderResponse)
                .toList();
        return new PagedResponseDTO<OrderResponse>(
                responseList,
                pagedResponse.getNumber(),
                pagedResponse.getSize(),
                pagedResponse.getTotalElements(),
                pagedResponse.getTotalPages(),
                pagedResponse.isLast(),
                pagedResponse.isFirst()
        );
    }

    public List<OrderItemResponse> getOrderItems(UUID orderId) {

        Order order = orderRepository.fetchByIdWithItemsAndProducts(orderId).orElseThrow(()-> {
                    throw new OrderNotFoundException(orderId.toString());
                }
        );

        if(!order.isActive()){
            throw new OrderNotFoundException(orderId.toString());
        }

        List<OrderItemResponse> itemResponses = order
                .getOrderItems()
                .stream()
                .map((item)->
                        new OrderItemResponse(item.getProduct().getProductId()
                                ,item.getProduct().getName()
                                ,item.getQuantity()
                                ,item.getItemTotal()))
                .toList();
        return itemResponses;
    }

    public PagedResponseDTO<OrderResponse> getMyOrders(User user,SearchRequest searchRequest) {
        Pageable pageRequest = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Order> pagedResponse = pagedResponse = orderRepository.findByUserAndActiveTrue(pageRequest,user);
        if(!StringUtils.isBlank(searchRequest.getQuery())){
            //immplement search feature later
        }
        List<Order> orders = pagedResponse.getContent();
        List<OrderResponse> responseList = orders.stream()
                .map(mapper::toOrderResponse)
                .toList();
        return new PagedResponseDTO<OrderResponse>(
                responseList,
                pagedResponse.getNumber(),
                pagedResponse.getSize(),
                pagedResponse.getTotalElements(),
                pagedResponse.getTotalPages(),
                pagedResponse.isLast(),
                pagedResponse.isFirst()
        );
    }

    @Transactional
    public Map<String,String> cancelOrder(User user, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new OrderNotFoundException(orderId.toString()));

        if(!order.isActive()){
            throw new OrderNotFoundException(orderId.toString());
        }

        if(!order.getUser().getUserId().equals(user.getUserId())){
            throw new InvalidOrderRequestException("You are not authorized to cancel this order");
        }

        if(!order.getStatus().equals(OrderStatus.PLACED)){
            throw new InvalidOrderRequestException("Cannot cancel order with status : "+order.getStatus());
        }

        if(!order.getOrderPaymentStatus().equals(OrderPaymentStatus.PAYMENT_PENDING)){
            throw new InvalidOrderRequestException("Cannot cancel order with status : "+order.getOrderPaymentStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_CANCELLED);
        for(var item : order.getOrderItems()){
            int rowsAffected = productRepository.rollbackStockAtomic(item.getProduct().getProductId(), item.getQuantity());
            if(rowsAffected == 0){
                log.error("Failed to rollback stock for product {} during order cancellation. Order ID: {}", item.getProduct().getProductId(), order.getOrderId());
                throw new InsufficientStockException("Failed to cancel order due to insufficient reserved stock for product: " + item.getProduct().getName());
            }
        }
        orderRepository.save(order);

        Map<String,String> response = new HashMap<>();
        response.put("message","Order cancelled successfully");
        return response;
    }
}
