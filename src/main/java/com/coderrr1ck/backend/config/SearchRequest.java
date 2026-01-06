package com.coderrr1ck.backend.config;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {
        @Min(value = 0,message = "Page number must be greater than or equal to 0")
        private int page = 0;

        @Min(value = 1,message = "Page size must be at least 1")
        @Max(value = 50,message = "Page size must be less than 50")
        private int size = 10;

        @Size(max = 50 ,message = "Search keyword is too long.")
        @Pattern(regexp = "^[a-zA-Z0-9\\s]+$",message = "Please provide valid search keyword")
        private String query;

        public String getQuery() {
                return query != null ? query.trim().toLowerCase() : null;
        }
}