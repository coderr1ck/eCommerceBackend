package com.coderrr1ck.backend.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.coderrr1ck.backend.productImage.ImageUploadFailed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
@Slf4j
public class CloudinaryUtil {

    private final Cloudinary cloudinary;
    private static final int MAX_RETRIES = 3;

    @Async
    public CompletableFuture<String> uploadFileToCloudinaryFolder(MultipartFile file, UUID productId, String originalFileName){
        if (file == null || productId == null) return CompletableFuture.failedFuture(new RuntimeException("Provided Image or Product ID is null"));
        log.info("Starting upload for: {} on thread: {}", originalFileName, Thread.currentThread().getName());

        String publicId = originalFileName.contains(".") && originalFileName.lastIndexOf('.') > 0
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                : originalFileName;

        Map<String, Object> options = ObjectUtils.asMap(
                "folder", "backend/products/" + productId,
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image"
        );

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                byte[] fileBytes = file.getBytes();
                Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, options);
                String uploadResultString = uploadResult.get("secure_url") != null ? uploadResult.get("secure_url").toString() : null;
                if(uploadResultString!=null || !uploadResultString.isEmpty()){
                    log.info("Successfully uploaded {} to Cloudinary on attempt {}. URL: {}", originalFileName, attempts, uploadResultString);
                }
                return CompletableFuture.completedFuture(uploadResultString);
            } catch (IOException io) {
                log.error("IO error uploading img to cloudinary {} on attempt {}", originalFileName, attempts, io);
                return CompletableFuture.failedFuture(io);
            } catch (Exception e) {
                e.printStackTrace();
                attempts++;
                log.error("Error uploading img to cloudinary {} on attempt {}", originalFileName, attempts);
                if(attempts>=MAX_RETRIES) return CompletableFuture.failedFuture(new ImageUploadFailed(e.getMessage()));
            }
        }
        log.error("Failed to upload image on cloudinary after {} attempts for product ID {}", MAX_RETRIES, productId);
        return CompletableFuture.failedFuture(new ImageUploadFailed("Failed to upload image after " + MAX_RETRIES + " attempts"));
    }


        /**
         * Deletes an image from Cloudinary using its secure URL.
         */
        @Async
        public void deleteFileFromCloudinary(String imageUrl) {
            log.info("Initiating deletion of image from Cloudinary. URL: {}", imageUrl);
            if (imageUrl == null || imageUrl.isEmpty()) return;

            try {
                String publicId = extractPublicIdFromUrl(imageUrl);
                log.info("Attempting to delete image with publicId: {}", publicId);

                Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

                if ("ok".equals(result.get("result"))) {
                    log.info("Successfully deleted image from Cloudinary: {}", publicId);
                } else {
                    log.warn("Cloudinary delete result for {}: {}", publicId, result.get("result"));
                }
            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary. URL: {}", imageUrl, e);
            }
        }



        private String extractPublicIdFromUrl(String url) {
            try {
                String marker = "/upload/";
                int startIndex = url.indexOf(marker) + marker.length();

                String path = url.substring(startIndex);
                if (path.startsWith("v")) {
                    path = path.substring(path.indexOf("/") + 1);
                }

                return path.substring(0, path.lastIndexOf("."));
            } catch (Exception e) {
                log.error("Failed to parse Public ID from URL: {}", url);
                throw new RuntimeException("Invalid Cloudinary URL format");
            }
        }

}
