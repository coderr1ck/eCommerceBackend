package com.coderrr1ck.backend.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;

    public String getName() {
        return name.trim().toLowerCase();
    }
}
