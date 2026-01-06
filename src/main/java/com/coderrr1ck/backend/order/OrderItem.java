package com.coderrr1ck.backend.order;

import com.coderrr1ck.backend.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @DocumentReference(collection = "products")
    private Product product;
    private Integer quantity;
    private BigDecimal itemTotal;
}
