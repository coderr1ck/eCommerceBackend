package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import com.coderrr1ck.backend.category.CategoryNotFoundException;
import com.coderrr1ck.backend.category.CategoryRepository;
import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    public PagedResponseDTO<ProductResponse> getAllProducts(SearchRequest searchRequest,UUID categoryId) {
        Pageable pageRequest = Pageable.ofSize(searchRequest.getSize()).withPage(searchRequest.getPage());

        Page<Product> pagedResponse = null;

        if(!StringUtils.isBlank(searchRequest.getQuery())){
            log.info("Product search query is :"+searchRequest.getQuery());
            pagedResponse = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchRequest.getQuery(),pageRequest);

        }else if(categoryId != null){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId.toString()));
            log.info("Fetching products for category :"+category.getName());
            pagedResponse = productRepository.findByCategoryAndActiveTrue(pageRequest,category);

        }else {
            pagedResponse = productRepository.findAllActiveProductsWithCategory(pageRequest);
        }

        List<Product> products = pagedResponse.getContent();
        List<ProductResponse> responseList = products.stream()
                .map(mapper::toProductResponse)
                .toList();

        return new PagedResponseDTO<>(
                responseList,
                pagedResponse.getNumber(),
                pagedResponse.getSize(),
                pagedResponse.getTotalElements(),
                pagedResponse.getTotalPages(),
                pagedResponse.isLast(),
                pagedResponse.isFirst()
        );
    }

    public ProductResponse saveProduct(ProductRequest productRequest) {
        if(productRepository.existsByNameAndActiveTrue(productRequest.getName())) {
            throw new ProductAlreadyExistsException(productRequest.getName());
        }
        Optional<Product> productInactive = productRepository.findByNameAndActiveFalse(productRequest.getName());

        if(productInactive.isPresent()){
            Product productExists = productInactive.get();
            Product updatedProduct = mapper.mapProductRequestToProduct(productRequest, productExists);
            updatedProduct.setActive(true);
            Product savedProduct = productRepository.save(updatedProduct);
            return mapper.toProductResponse(savedProduct);
        }
        Product product = mapper.toProduct(productRequest);
        Product savedProduct = productRepository.save(product);
        return mapper.toProductResponse(savedProduct);
    }


    public ProductResponse updateProduct(ProductRequest productRequest, UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));

        if(!product.isActive()){
            throw new ProductNotFoundException(product.getProductId().toString());
        }

        Optional<Product> existingProductByNameActive = productRepository.findByNameAndActiveTrue(productRequest.getName());
        if(existingProductByNameActive.isPresent() && !existingProductByNameActive.get().getProductId().equals(id)) {
            throw new ProductAlreadyExistsException(productRequest.getName());
        }

        Optional<Product> existingProductByName = productRepository.findByNameAndActiveFalse(productRequest.getName());
        if(existingProductByName.isPresent() && !existingProductByName.get().getProductId().equals(id)) {
            throw new ProductAlreadyExistsException(productRequest.getName());
        }

        Product updatedProduct = mapper.mapProductRequestToProduct(productRequest, product);
        Product updatedSavedProduct = productRepository.save(updatedProduct);
        return mapper.toProductResponse(updatedSavedProduct);
    }


    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        product.setActive(false);
        product.getImages().clear();
        productRepository.save(product);
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findByIdWithCategoryAndImages(id).orElseThrow(
                ()-> new ProductNotFoundException(id.toString()));
        if(!product.isActive()){
            throw new ProductNotFoundException(product.getProductId().toString());
        }
        return mapper.toSingleProductResponse(product);
    }

}
