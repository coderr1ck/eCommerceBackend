package com.coderrr1ck.backend.product;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @NotNull
    private String name;

    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    @NotNull
    private BigDecimal price;

    @NotNull
    private UUID categoryId;

    @Min(value = 0, message = "Product stock cannot be negative")
    @NotNull
    private Integer stock;

    public String getName() {
        return name.trim().toLowerCase();
    }

}
