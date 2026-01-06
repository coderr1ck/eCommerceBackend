package com.coderrr1ck.backend.category;

import com.coderrr1ck.backend.config.SearchRequest;
import com.coderrr1ck.backend.config.PagedResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CategoryResponse> saveCategory(
           @Valid @RequestBody  CategoryRequest categoryRequest
    ) {
        CategoryResponse response = categoryService.saveCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable("id") String id,
            @Valid @RequestBody CategoryRequest categoryRequest
    ){
        return ResponseEntity.ok(categoryService.updateCategory(id,categoryRequest));
    }

//    204 response
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable("id") String id
    ) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }


}
