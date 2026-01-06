package com.coderrr1ck.backend.product;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @NotBlank(message = "Category ID is required")
    @Pattern(regexp = "^[0-9a-fA-F]{24}$", message = "Please provide valid Category ID")
    private String categoryId;

    @Min(value = 0, message = "Product stock cannot be negative")
    private Integer stock;

    public String getName() {
        return name.trim().toLowerCase();
    }

}
