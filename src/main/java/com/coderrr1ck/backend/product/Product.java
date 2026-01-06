package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    private String productId;

    @Indexed(unique = true)
    @NotNull
    private String name;

    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    @NotNull
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @NotNull
    private Integer stock;

    @NotNull
    private String categoryId;

    private Boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
