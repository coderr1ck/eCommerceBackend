package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import com.coderrr1ck.backend.category.CategoryNotFoundException;
import com.coderrr1ck.backend.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryRepository categoryRepository;

    public Product toProduct(ProductRequest productRequest) {
        if(!categoryRepository.existsByCategoryIdAndActiveTrue(productRequest.getCategoryId())) {
            throw new CategoryNotFoundException(productRequest.getCategoryId());
        }
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setCategoryId(productRequest.getCategoryId());
        return product;
    }

    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        Optional<Category> category = categoryRepository.findById(product.getCategoryId());
        category.ifPresent(value -> response.setCategory(value.getName()));
        return response;
    }

    public Product mapProductRequestToProduct(ProductRequest productRequest, Product product) {
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setStock(productRequest.getStock());
        product.setPrice(productRequest.getPrice());
        return product;
    }
}
