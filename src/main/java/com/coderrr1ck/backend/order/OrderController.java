package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> saveOrder(
            @Valid @RequestBody OrderRequest orderRequest
    ) {
        return ResponseEntity.ok(orderService.saveOrder(orderRequest));
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<OrderResponse>> getAllOrders(
            @Valid SearchRequest searchRequest
            ) {
        return ResponseEntity.ok(orderService.getAllOrders(searchRequest));
    }
}
