package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.productImage.ProductImageResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String primaryImageUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ProductImageResponse> images;
}
