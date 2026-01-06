package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

       private final ProductService productService;

//       apply pagination later
       @GetMapping
       public ResponseEntity<PagedResponseDTO<ProductResponse>> getAllProducts(
               @Valid SearchRequest searchRequest
               ) {
           PagedResponseDTO<ProductResponse> allProducts = productService.getAllProducts(searchRequest);
           return ResponseEntity.ok(allProducts);
       }

         @GetMapping("{id}")
         public ResponseEntity<ProductResponse> getProductById(
                 @PathVariable("id") String id
         ) {
             ProductResponse productResponse = productService.getProductById(id);
             return ResponseEntity.ok(productResponse);
         }


       @PostMapping
       public ResponseEntity<ProductResponse> saveProduct(
               @Valid @RequestBody ProductRequest productRequest
       ) {
           ProductResponse savedProduct = productService.saveProduct(productRequest);
           return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
       }

        @PutMapping("{id}")
        public ResponseEntity<ProductResponse> updateProduct(
                @Valid @RequestBody ProductRequest productRequest,
                @PathVariable("id") String id
        ) {
            ProductResponse savedProduct = productService.updateProduct(productRequest,id);
            return ResponseEntity.ok(savedProduct);
        }

        @DeleteMapping("{id}")
        public ResponseEntity<Void> deleteProduct(
                @PathVariable("id") String id
        ) {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }

}
