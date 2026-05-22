package com.coderrr1ck.backend.category;

import com.coderrr1ck.backend.config.SearchRequest;
import com.coderrr1ck.backend.config.PagedResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<PagedResponseDTO<CategoryResponse>> getAllCategories(
            @Valid SearchRequest searchRequest
    ) {
        PagedResponseDTO<CategoryResponse> allCategories = categoryService.getAllCategories(searchRequest);
        return ResponseEntity.ok(allCategories);
    }

//    201 response
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> saveCategory(
           @Valid @RequestBody  CategoryRequest categoryRequest
    ) {
        CategoryResponse response = categoryService.saveCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CategoryRequest categoryRequest
    ){
        return ResponseEntity.ok(categoryService.updateCategory(id,categoryRequest));
    }

//    204 response
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable("id") UUID id
    ) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }


}
