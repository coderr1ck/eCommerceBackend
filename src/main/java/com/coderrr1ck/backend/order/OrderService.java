package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.category.Category;
import com.coderrr1ck.backend.category.CategoryResponse;
import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import com.coderrr1ck.backend.product.Product;
import com.coderrr1ck.backend.product.ProductRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;


    public OrderResponse saveOrder(OrderRequest orderRequest) {
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new InvalidOrderRequestException("Order request items cannot be empty");
        }
        Map<String, Integer> qtyByProductId = orderRequest.getItems().stream()
                .collect(Collectors.groupingBy(
                        OrderItemRequest::getProductId,
                        Collectors.summingInt(OrderItemRequest::getQuantity)
                ));

        Set<String> productIds = qtyByProductId.keySet();

        List<Product> products = productRepository.findAllById(productIds).stream()
                .filter((product -> product.getActive()))
                .toList();

        if (products.size() != productIds.size()) {
            throw new ProductNotFoundInOrder("One or more products in the order were not found");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> itemList = new ArrayList<>();
        List<Product> reservedProducts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : qtyByProductId.entrySet()) {
            String productId = entry.getKey();
            int qty = entry.getValue();

            if (qty <= 0) {
                throw new InvalidOrderRequestException("Invalid qty " + qty + " for " + productId);
            }
            if (productId == null) {
                throw new InvalidOrderRequestException("Product ID cannot be null");
            }

            Product reservedProduct = reserveStock(productId, qty,false);

            if (reservedProduct == null) {
                rollbackStock(reservedProducts, qtyByProductId);
                throw new InsufficientStockException("Insufficient stock for product ID " + productId);
            }

            BigDecimal unitPrice = reservedProduct.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            itemList.add(new OrderItem(reservedProduct, qty, itemTotal));
            totalAmount = totalAmount.add(itemTotal);
            reservedProducts.add(reservedProduct);
        }
        Order order = mapper.toOrder(orderRequest, itemList, totalAmount);
        Order savedOrder = orderRepository.save(order);
        return mapper.toOrderResponse(savedOrder);
    }

    private void rollbackStock(List<Product> reservedProducts, Map<String, Integer> qtyByProductId) {
        for(Product product:reservedProducts){
            String productId = product.getProductId();
            int qty = qtyByProductId.get(productId);
            reserveStock(productId, qty,true);
        }
    }

    private Product reserveStock(String productId, int qty,boolean rollback) {
        Query query;
        Update update;

        if (!rollback) {
            log.info("Reserving stock for productId: {} , qty: {}", productId, qty);
            query = new Query(Criteria.where("productId").is(productId)
                    .and("stock").gte(qty)
                    .and("active").is(true));

            update = new Update().inc("stock", -qty);
        } else {
            log.info("Rolling back reserved stock for productId: {} , qty: {}", productId, qty);
            query = new Query(Criteria.where("productId").is(productId)
                    .and("active").is(true));

            update = new Update().inc("stock", qty);
        }

        return mongoTemplate.findAndModify(
                query, update,
                FindAndModifyOptions.options().returnNew(true),
                Product.class
        );
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
}
