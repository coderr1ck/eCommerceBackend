package com.coderrr1ck.backend.category;

import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public PagedResponseDTO<CategoryResponse> getAllCategories(SearchRequest searchRequest) {
        Pageable pageRequest = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Category> pagedResponse = null;
        if(!StringUtils.isBlank(searchRequest.getQuery())){
            System.out.println("Category search query is :"+searchRequest.getQuery());
            pagedResponse = categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchRequest.getQuery(),pageRequest);
            System.out.println("Returning category searched query paged response");
        }else{
            pagedResponse = categoryRepository.findByActiveTrue(pageRequest);
        }
        List<Category> categories = pagedResponse.getContent();
        List<CategoryResponse> responseList = categories.stream()
                .map(mapper::toCategoryResponse)
                .toList();
        return new PagedResponseDTO<CategoryResponse>(
                responseList,
                pagedResponse.getNumber(),
                pagedResponse.getSize(),
                pagedResponse.getTotalElements(),
                pagedResponse.getTotalPages(),
                pagedResponse.isLast(),
                pagedResponse.isFirst()
        );
    }

    public CategoryResponse saveCategory(CategoryRequest categoryRequest) {
        if(categoryRepository.existsByNameAndActiveTrue(categoryRequest.getName())) {
            throw new CategoryAlreadyExistsException(categoryRequest.getName());
        }
        Optional<Category> category = categoryRepository.findByNameAndActiveFalse(categoryRequest.getName());
        if(category.isPresent()){
            Category categoryExists = category.get();
            categoryExists.setActive(true);
            categoryExists.setDescription(categoryRequest.getDescription());
            return mapper.toCategoryResponse(categoryRepository.save(categoryExists));
        }else {
            Category categorytoSave = mapper.toCategory(categoryRequest);
            Category savedCategory = categoryRepository.save(categorytoSave);
            return mapper.toCategoryResponse(savedCategory);
        }
    }

    public CategoryResponse updateCategory(UUID id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        if(!category.isActive()){
            throw new CategoryNotFoundException(id.toString());
        }
        if(categoryRepository.existsByNameAndActiveTrue(categoryRequest.getName())) {
            throw new CategoryAlreadyExistsException(categoryRequest.getName());
        }
        Category updatedCategory = mapper.mapCategoryRequesttoCategory(category,categoryRequest);
        CategoryResponse response = mapper.toCategoryResponse(categoryRepository.save(updatedCategory));
        return response;
    }

    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id.toString()));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
