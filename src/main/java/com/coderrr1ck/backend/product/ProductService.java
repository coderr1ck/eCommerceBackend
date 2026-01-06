package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.category.Category;
import com.coderrr1ck.backend.category.CategoryNotFoundException;
import com.coderrr1ck.backend.category.CategoryRepository;
import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    public PagedResponseDTO<ProductResponse> getAllProducts(SearchRequest searchRequest) {
        Pageable pageRequest = Pageable.ofSize(searchRequest.getSize()).withPage(searchRequest.getPage());
        Page<Product> pagedResponse = null;
        if(!StringUtils.isBlank(searchRequest.getQuery())){
            System.out.println("Product search query is :"+searchRequest.getQuery());
            String keyword = ".*"+searchRequest.getQuery()+".*";
            pagedResponse = productRepository.findByNameRegexAndActiveTrue(keyword,pageRequest);
            System.out.println("Returning product searched query paged response");
        }else {
            pagedResponse = productRepository.findByActiveTrue(pageRequest);
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
            productExists.setActive(true);
            return mapper.toProductResponse(productRepository.save(productExists));
        }
        Product product = mapper.toProduct(productRequest);
        Product savedProduct = productRepository.save(product);
        return mapper.toProductResponse(savedProduct);
    }


    public ProductResponse updateProduct(ProductRequest productRequest, String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if(product.getActive()==false){
            throw new ProductNotFoundException(product.getProductId());
        }

        if(productRepository.existsByNameAndActiveTrue(productRequest.getName())) {
            throw new ProductAlreadyExistsException(productRequest.getName());
        }

        Product updatedProduct = mapper.mapProductRequestToProduct(productRequest, product);
        Product updatedSavedProduct = productRepository.save(updatedProduct);
        return mapper.toProductResponse(updatedSavedProduct);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setActive(false);
        productRepository.save(product);
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id).orElseThrow(
                ()-> new ProductNotFoundException(id));
        if(product.getActive() == false){
            throw new ProductNotFoundException(product.getProductId());
        }
        return mapper.toProductResponse(product);
    }
}
