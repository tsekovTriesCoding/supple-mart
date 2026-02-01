package app.cloudinary;

import app.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryService(cloudinary);
    }

    @Nested
    @DisplayName("Upload Image Tests")
    class UploadImageTests {

        @Test
        @DisplayName("Should upload valid image successfully")
        void shouldUploadValidImage() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test/image.jpg"));

            CompletableFuture<String> result = cloudinaryService.uploadImage(file, "products");

            assertThat(result.get()).isEqualTo("https://res.cloudinary.com/test/image.jpg");
        }

        @Test
        @DisplayName("Should throw exception for null file")
        void shouldThrowExceptionForNullFile() {
            assertThatThrownBy(() -> cloudinaryService.uploadImage(null, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("select a file");
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("select a file");
        }

        @Test
        @DisplayName("Should throw exception for file exceeding size limit")
        void shouldThrowExceptionForLargeFile() {
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    largeContent
            );

            assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("5MB");
        }

        @Test
        @DisplayName("Should throw exception for invalid file extension")
        void shouldThrowExceptionForInvalidExtension() {
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.pdf",
                    "image/jpeg",
                    "test content".getBytes()
            );

            assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid file type");
        }

        @Test
        @DisplayName("Should throw exception for non-image content type")
        void shouldThrowExceptionForNonImageContentType() {
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "application/pdf",
                    "test content".getBytes()
            );

            assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("must be an image");
        }

        @Test
        @DisplayName("Should handle Cloudinary upload failure")
        void shouldHandleCloudinaryUploadFailure() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Failed to upload");
        }

        @Test
        @DisplayName("Should accept valid image extensions")
        void shouldAcceptValidExtensions() throws Exception {
            String[] validExtensions = {"jpg", "jpeg", "png", "gif", "webp"};

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenReturn(Map.of("secure_url", "https://example.com/image.jpg"));

            for (String ext : validExtensions) {
                MockMultipartFile file = new MockMultipartFile(
                        "image",
                        "test." + ext,
                        "image/" + ext,
                        "test content".getBytes()
                );

                CompletableFuture<String> result = cloudinaryService.uploadImage(file, "products");
                assertThat(result.get()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Delete Image Tests")
    class DeleteImageTests {

        @Test
        @DisplayName("Should delete image successfully")
        void shouldDeleteImageSuccessfully() throws Exception {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(anyString(), any(Map.class)))
                    .thenReturn(Map.of("result", "ok"));

            cloudinaryService.deleteImage("products/test-image");

            verify(uploader).destroy(eq("products/test-image"), any(Map.class));
        }

        @Test
        @DisplayName("Should handle delete failure gracefully")
        void shouldHandleDeleteFailureGracefully() throws Exception {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(anyString(), any(Map.class)))
                    .thenThrow(new IOException("Delete failed"));

            // Should not throw - just logs the error
            cloudinaryService.deleteImage("products/test-image");

            verify(uploader).destroy(eq("products/test-image"), any(Map.class));
        }
    }

    @Nested
    @DisplayName("Extract Public ID Tests")
    class ExtractPublicIdTests {

        @Test
        @DisplayName("Should extract public ID from Cloudinary URL")
        void shouldExtractPublicId() {
            String url = "https://res.cloudinary.com/demo/image/upload/v1234567890/products/test-image.jpg";

            String publicId = cloudinaryService.extractPublicId(url);

            assertThat(publicId).isEqualTo("products/test-image");
        }

        @Test
        @DisplayName("Should return null for null URL")
        void shouldReturnNullForNullUrl() {
            assertThat(cloudinaryService.extractPublicId(null)).isNull();
        }

        @Test
        @DisplayName("Should return null for empty URL")
        void shouldReturnNullForEmptyUrl() {
            assertThat(cloudinaryService.extractPublicId("")).isNull();
        }

        @Test
        @DisplayName("Should return null for non-Cloudinary URL")
        void shouldReturnNullForNonCloudinaryUrl() {
            assertThat(cloudinaryService.extractPublicId("https://example.com/image.jpg")).isNull();
        }

        @Test
        @DisplayName("Should return null for invalid Cloudinary URL format")
        void shouldReturnNullForInvalidFormat() {
            assertThat(cloudinaryService.extractPublicId("https://res.cloudinary.com/image.jpg")).isNull();
        }
    }

    @Nested
    @DisplayName("Is Cloudinary URL Tests")
    class IsCloudinaryUrlTests {

        @Test
        @DisplayName("Should return true for Cloudinary URL")
        void shouldReturnTrueForCloudinaryUrl() {
            assertThat(cloudinaryService.isCloudinaryUrl("https://res.cloudinary.com/demo/image.jpg")).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-Cloudinary URL")
        void shouldReturnFalseForNonCloudinaryUrl() {
            assertThat(cloudinaryService.isCloudinaryUrl("https://example.com/image.jpg")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null URL")
        void shouldReturnFalseForNullUrl() {
            assertThat(cloudinaryService.isCloudinaryUrl(null)).isFalse();
        }
    }
}
