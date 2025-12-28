package app.cloudinary;

import app.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Async
    public CompletableFuture<String> uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "format", "jpg",
                    "quality", "auto",
                    "fetch_format", "auto"
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = (String) uploadResult.get("secure_url");

            log.info("Image uploaded successfully to Cloudinary: {}", imageUrl);
            return CompletableFuture.completedFuture(imageUrl);

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    @Async
    @SuppressWarnings("unchecked")
    public void deleteImage(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary: {}", publicId, e);
            // Don't throw exception in async fire-and-forget operation, just log it
        }
    }

    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || !isCloudinaryUrl(imageUrl)) {
            return null;
        }

        String[] parts = imageUrl.split("/upload/");
        if (parts.length < 2) {
            return null;
        }

        String pathAfterUpload = parts[1];
        pathAfterUpload = pathAfterUpload.replaceFirst("v\\d+/", "");
        int lastDotIndex = pathAfterUpload.lastIndexOf('.');
        if (lastDotIndex > 0) {
            pathAfterUpload = pathAfterUpload.substring(0, lastDotIndex);
        }

        return pathAfterUpload;
    }

    public boolean isCloudinaryUrl(String imageUrl) {
        return imageUrl != null && imageUrl.contains("cloudinary.com");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new BadRequestException("Invalid file type. Allowed types: jpg, jpeg, png, gif, webp");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }
    }

    private boolean hasValidExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
