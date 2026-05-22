package com.coderrr1ck.backend.product;

import com.coderrr1ck.backend.config.PagedResponseDTO;
import com.coderrr1ck.backend.config.SearchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

       private final ProductService productService;

//       apply pagination later
       @GetMapping
       public ResponseEntity<PagedResponseDTO<ProductResponse>> getAllProducts(
               @Valid SearchRequest searchRequest,
               @RequestParam(value = "categoryId" , required = false)
               UUID categoryId
               ) {
           PagedResponseDTO<ProductResponse> allProducts = productService.getAllProducts(searchRequest,categoryId);
           return ResponseEntity.ok(allProducts);
       }

         @GetMapping("{id}")
         public ResponseEntity<ProductResponse> getProductById(
                 @PathVariable("id") UUID id
         ) {
             ProductResponse productResponse = productService.getProductById(id);
             return ResponseEntity.ok(productResponse);
         }


       @PostMapping
       @PreAuthorize("hasRole('ADMIN')")
       public ResponseEntity<ProductResponse> saveProduct(
               @Valid @RequestBody ProductRequest productRequest
       ) {
           ProductResponse savedProduct = productService.saveProduct(productRequest);
           return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
       }


        @PutMapping("{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ProductResponse> updateProduct(
                @Valid @RequestBody ProductRequest productRequest,
                @PathVariable("id") UUID id
        ) {
            ProductResponse savedProduct = productService.updateProduct(productRequest,id);
            return ResponseEntity.ok(savedProduct);
        }

        @DeleteMapping("{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteProduct(
                @PathVariable("id") UUID id
        ) {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }


}
