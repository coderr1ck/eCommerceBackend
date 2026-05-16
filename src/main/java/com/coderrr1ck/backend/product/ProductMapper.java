package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import com.coderrr1ck.backend.category.CategoryNotFoundException;
import com.coderrr1ck.backend.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryRepository categoryRepository;

    public Product toProduct(ProductRequest productRequest) {

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(productRequest.getCategoryId().toString()));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setAvailableStock(productRequest.getStock());
        product.setCategory(category);
        return product;
    }

    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getAvailableStock());
        response.setCategory(product.getCategory().getName());
        response.setPrimaryImageUrl(product.getPrimaryImage() != null ? product.getPrimaryImage().getUrl() : null);
        return response;
    }

    public ProductResponse toSingleProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getAvailableStock());
        response.setCategory(product.getCategory().getName());
        response.setImageUrls(product
                .getImages()
                .stream()
                .map((img)->img.getUrl())
                .toList());;
        return response;
    }

    public Product mapProductRequestToProduct(ProductRequest productRequest, Product product) {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(productRequest.getCategoryId().toString()));
        product.setName(productRequest.getName());
        product.setCategory(category);
        product.setDescription(productRequest.getDescription());
        product.setAvailableStock(productRequest.getStock());
        product.setPrice(productRequest.getPrice());
        return product;
    }
}
