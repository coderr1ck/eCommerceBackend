package com.coderrr1ck.backend.order;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "User id is required")
    @NotBlank(message = "User id is required")
    @Pattern(regexp = "^[0-9a-fA-F]{24}$", message = "Please provide valid user id")
    private String orderId;

    @Valid
    @NotEmpty(message = "Order must contain at least one [{ item }] ")
    @Size(min = 1, message = "Order [{ items... }] cannot be empty")
    private List<OrderItemRequest> items;

    @NotBlank(message = "Customer name is required")
    private String customerName;
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

}
