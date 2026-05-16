package com.coderrr1ck.backend.utils;

import com.coderrr1ck.backend.productImage.InvalidImageFile;
import com.coderrr1ck.backend.product.Product;
import com.coderrr1ck.backend.productImage.ProductImage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FileUtil {

    private final CloudinaryUtil cloudinaryUtil;

    private String getFileExtension(MultipartFile file){
        String fileName = file.getOriginalFilename();
        if(fileName == null || !fileName.contains(".") ){
            throw new InvalidImageFile("File has no extension " + fileName);
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    public void validateImageFile(MultipartFile file){
        if (file.isEmpty()) {
            throw new InvalidImageFile("Provided Image File is empty");
        }
        String extension = getFileExtension(file);
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".svg");

        if (!allowedExtensions.contains(extension)) {
            throw new InvalidImageFile("Extension " + extension + " is not supported");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageFile("File is not a valid image type");
        }
    }

    public MultipartFile[] validateAndFilterUniqueImageFiles(MultipartFile[] files, Product product) {

        for(MultipartFile file : files){
            validateImageFile(file);
        }

        Collection<MultipartFile> uniqueFiles = Arrays.stream(files)
                .collect(Collectors.toMap(
                        MultipartFile::getOriginalFilename,
                        f -> f,
                        (existing, replacement) -> existing
                ))
                .values();

        Set<String> dbNames = product.getImages().stream()
                .map(ProductImage::getOriginalFilename)
                .collect(Collectors.toSet());

        return uniqueFiles.stream()
                .filter(f -> !dbNames.contains(f.getOriginalFilename()))
                .toArray(MultipartFile[]::new);
    }

    public Map<String, String> uploadMultipleImageFileAsync(MultipartFile[] files, UUID productId) {
        List<CompletableFuture<Map.Entry<String, String>>> uploadFutures = Arrays.stream(files)
                .map(file -> {
                    String originalFileName = file.getOriginalFilename();

                    String fileName = originalFileName != null || !originalFileName.isEmpty() ?
                            originalFileName : "image_" + productId.toString().substring(0, 8) + "_" + System.currentTimeMillis();

                    return cloudinaryUtil.uploadFileToCloudinaryFolder(file,productId,fileName)
                            .thenApply(url -> Map.entry(fileName, url)) // Map URL to Entry(FileName, URL)
                            .exceptionally(ex -> {
                                ex.printStackTrace();
                                log.error("Bulk Processor, Failed to upload image {}: {}", fileName, ex.getMessage());
                                return null;
                            });
                })
                .toList();

        CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                uploadFutures.toArray(new CompletableFuture[0])
        );

        return allUploads.thenApply(v ->
                uploadFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ))
        ).join();
    }

    public CompletableFuture<String> uploadSingleImageFileAsync(MultipartFile file, UUID productId) {
        String originalFileName = file.getOriginalFilename();
        String fileName = originalFileName != null || !originalFileName.isEmpty() ?
                originalFileName : "image_" + productId.toString().substring(0, 8) + "_" + System.currentTimeMillis();

        return cloudinaryUtil.uploadFileToCloudinaryFolder(file, productId, fileName)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    log.error("Failed to upload image single async {}: {}", fileName, ex.getMessage());
                    return null;
                });
    }
}
