package com.coderrr1ck.backend.productImage;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/images")
@AllArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(path = "upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> uploadProductImage(
            @PathVariable("id") UUID id,
            @RequestParam("files") MultipartFile[] files
    ) {
        String response = productImageService.uploadProductImage(id, files);
        return ResponseEntity.ok(Map.of("message",response));
    }

    @PutMapping(path = "update/{id}/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String ,String>> updateProductImage(
            @PathVariable("id") UUID id,
            @PathVariable("imageId") Integer imageId,
            @RequestParam("file") MultipartFile file
    ) {
        String response = productImageService.updateProductImage(id,imageId, file);
        return ResponseEntity.ok(Map.of("message",response));
    }

    @PutMapping(path = "set-primary/{id}/{imageId}")
    public ResponseEntity<Map<String ,String>> setProductImagePrimary(
            @PathVariable("id") UUID id,
            @PathVariable("imageId") Integer imageId
    ) {
        String response = productImageService.setProductImagePrimary(id,imageId);
        return ResponseEntity.ok(Map.of("message",response));
    }


    @DeleteMapping(path = "{id}/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable("id") UUID id,
            @PathVariable("imageId") Integer imageId
    ) {
        productImageService.deleteProductImage(id,imageId);
        return ResponseEntity.noContent().build();
    }
}
