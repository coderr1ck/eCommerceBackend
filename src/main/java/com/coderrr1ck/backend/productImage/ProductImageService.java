package com.coderrr1ck.backend.productImage;

import com.coderrr1ck.backend.product.Product;
import com.coderrr1ck.backend.product.ProductNotFoundException;
import com.coderrr1ck.backend.product.ProductRepository;
import com.coderrr1ck.backend.utils.CloudinaryUtil;
import com.coderrr1ck.backend.utils.FileUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final FileUtil fileUtil;
    private final CloudinaryUtil cloudinaryUtil;

    public String uploadProductImage(UUID id, MultipartFile[] files) {
        if(files == null || files.length == 0){
            throw new InvalidImageFile("No image files were provided for upload");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));

        if(!product.isActive()){
            throw new ProductNotFoundException(id.toString());
        }

        MultipartFile[] finalBatch = fileUtil.validateAndFilterUniqueImageFiles(files,product);

        if (finalBatch.length == 0) throw new ImagesAlreadyUploded("No new images to upload .");

        Map<String,String> uploadedUrls = fileUtil.uploadMultipleImageFileAsync(finalBatch, id);

        if(uploadedUrls.isEmpty()){
            throw new ImageUploadFailed("Failed to upload images , please try again later");
        }else{
            for(Map.Entry<String,String> entry : uploadedUrls.entrySet()){
                product.addImage(ProductImage
                        .builder()
                        .originalFilename(entry.getKey())
                        .url(entry.getValue())
                        .build());
            }
            productRepository.save(product);
            return "Successfully uploaded "+uploadedUrls.size()+" out of "+finalBatch.length+" images";
        }
    }

    public String updateProductImage(UUID id,Integer imageId, MultipartFile file) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        if(!product.isActive()){
            throw new ProductNotFoundException(id.toString());
        }
        fileUtil.validateImageFile(file);

        List<ProductImage> images = product.getImages();

        ProductImage imageToUpdate = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ProductImageNotFoundException(imageId.toString()));


        boolean isPresent = images.stream().anyMatch((img) -> img.getOriginalFilename().equals(file.getOriginalFilename()));

        if(isPresent){
            throw new ImagesAlreadyUploded("An image with the same name already exists for this product");
        }

        CompletableFuture<String> futureUrl = fileUtil.uploadSingleImageFileAsync(file,product.getProductId());
        String uploadedUrl = futureUrl.join();

        if(uploadedUrl == null){
            throw new ImageUploadFailed("Failed to upload image , please try again later");
        }
        String oldUrl = imageToUpdate.getUrl();
        imageToUpdate.setOriginalFilename(file.getOriginalFilename());
        imageToUpdate.setUrl(uploadedUrl);
        Product savedProduct = productRepository.save(product);
        if(savedProduct!=null){
            cloudinaryUtil.deleteFileFromCloudinary(oldUrl);
        }
        return "Successfully updated image with id "+imageId;
    }

    public void deleteProductImage(UUID id, Integer imageId) {
        Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        if(!product.isActive()){
            throw new ProductNotFoundException(id.toString());
        }

        ProductImage imageToRemove = product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ProductImageNotFoundException(imageId.toString()));

        product.getImages().remove(imageToRemove);
        productRepository.save(product);
        cloudinaryUtil.deleteFileFromCloudinary(imageToRemove.getUrl());
    }

    public String setProductImagePrimary(UUID id, Integer imageId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
        if(!product.isActive()){
            throw new ProductNotFoundException(id.toString());
        }
        ProductImage imageToSetPrimary = product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ProductImageNotFoundException(imageId.toString()));
        product.setPrimaryImage(imageToSetPrimary);
        productRepository.save(product);
        return "Successfully set image with id "+imageId+" as primary image for product .";
    }
}
