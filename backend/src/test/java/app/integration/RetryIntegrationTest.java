package app.integration;

import app.cloudinary.CloudinaryService;
import app.exception.BadRequestException;
import app.notification.service.EmailService;
import app.notification.service.EmailTemplateService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        CloudinaryService.class,
        EmailService.class
})
@EnableRetry
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.email.from=test@supplemart.com"
})
@DisplayName("Retry Integration Tests")
class RetryIntegrationTest {

    @MockitoBean
    private Cloudinary cloudinary;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private EmailTemplateService emailTemplateService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmailService emailService;

    private Uploader uploader;

    @Nested
    @DisplayName("CloudinaryService Retry Tests")
    class CloudinaryRetryTests {

        @Test
        @DisplayName("Should retry on IOException and succeed on third attempt")
        void shouldRetryOnIOExceptionAndSucceed() throws Exception {
            MockMultipartFile file = createValidImageFile();
            uploader = mock(Uploader.class);
            reset(cloudinary);
            
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Network error"))
                    .thenThrow(new IOException("Network error again"))
                    .thenReturn(Map.of("secure_url", "https://cloudinary.com/success.jpg"));

            CompletableFuture<String> result = cloudinaryService.uploadImage(file, "products");

            assertThat(result.get()).isEqualTo("https://cloudinary.com/success.jpg");
            verify(uploader, times(3)).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should retry on network errors and succeed on second attempt")
        void shouldRetryOnNetworkErrorAndSucceed() throws Exception {
            MockMultipartFile file = createValidImageFile();
            uploader = mock(Uploader.class);
            reset(cloudinary);
            
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Connection timed out"))
                    .thenReturn(Map.of("secure_url", "https://cloudinary.com/success.jpg"));

            CompletableFuture<String> result = cloudinaryService.uploadImage(file, "products");

            assertThat(result.get()).isEqualTo("https://cloudinary.com/success.jpg");
            verify(uploader, times(2)).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should retry multiple times on persistent errors")
        void shouldRetryMultipleTimesOnPersistentErrors() throws Exception {
            MockMultipartFile file = createValidImageFile();
            uploader = mock(Uploader.class);
            reset(cloudinary);
            
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Connection refused"))
                    .thenThrow(new IOException("Connection refused"))
                    .thenReturn(Map.of("secure_url", "https://cloudinary.com/success.jpg"));

            CompletableFuture<String> result = cloudinaryService.uploadImage(file, "products");

            assertThat(result.get()).isEqualTo("https://cloudinary.com/success.jpg");
            verify(uploader, times(3)).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should call recovery method after all retries exhausted")
        void shouldCallRecoveryAfterAllRetriesExhausted() throws Exception {
            MockMultipartFile file = createValidImageFile();
            uploader = mock(Uploader.class);
            reset(cloudinary);
            
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Persistent network error"));

            assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Failed to upload image after multiple attempts");

            verify(uploader, times(4)).upload(any(byte[].class), any(Map.class));
        }

        @Test
        @DisplayName("Should NOT retry on validation errors - invalid file type")
        void shouldNotRetryOnValidationError() {
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "document.txt",
                    "text/plain",
                    "This is not an image".getBytes()
            );
            uploader = mock(Uploader.class);
            reset(cloudinary);
            
            assertThatThrownBy(() -> cloudinaryService.uploadImage(invalidFile, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid file type");

            verifyNoInteractions(uploader);
        }

        @Test
        @DisplayName("Should NOT retry on validation errors - empty file")
        void shouldNotRetryOnEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );
            uploader = mock(Uploader.class);
            reset(cloudinary);

            assertThatThrownBy(() -> cloudinaryService.uploadImage(emptyFile, "products"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Please select a file to upload");

            verifyNoInteractions(uploader);
        }

        private MockMultipartFile createValidImageFile() {
            return new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
        }
    }

    @Nested
    @DisplayName("EmailService Retry Tests")
    class EmailRetryTests {

        @Test
        @DisplayName("Should retry on MessagingException and succeed on second attempt")
        void shouldRetryOnMessagingExceptionAndSucceed() throws Exception {
            reset(mailSender);
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            
            doThrow(new MailSendException("SMTP connection failed"))
                    .doNothing()
                    .when(mailSender).send(any(MimeMessage.class));

            emailService.sendEmail("test@example.com", "Test Subject", "<p>Test Content</p>");

            verify(mailSender, times(2)).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should retry on MailSendException up to 3 times")
        void shouldRetryUpToThreeTimes() throws Exception {
            reset(mailSender);
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            
            doThrow(new MailSendException("SMTP server unavailable"))
                    .doThrow(new MailSendException("SMTP server unavailable"))
                    .doNothing()
                    .when(mailSender).send(any(MimeMessage.class));

            emailService.sendEmail("test@example.com", "Test Subject", "<p>Test Content</p>");

            verify(mailSender, times(3)).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("Should gracefully handle exhausted retries without throwing")
        void shouldGracefullyHandleExhaustedRetries() throws Exception {
            reset(mailSender);
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            
            doThrow(new MailSendException("Persistent SMTP failure"))
                    .when(mailSender).send(any(MimeMessage.class));

            emailService.sendEmail("test@example.com", "Test Subject", "<p>Test Content</p>");

            verify(mailSender, times(3)).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("Retry Backoff Tests")
    class RetryBackoffTests {

        @Test
        @DisplayName("Should apply exponential backoff between retries")
        void shouldApplyExponentialBackoff() throws Exception {
            uploader = mock(Uploader.class);
            reset(cloudinary);
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", "test".getBytes()
            );
            
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Network error"))
                    .thenReturn(Map.of("secure_url", "https://cloudinary.com/success.jpg"));

            long startTime = System.currentTimeMillis();
            CompletableFuture<String> result = cloudinaryService.uploadImage(file, "products");
            result.get();
            long elapsedTime = System.currentTimeMillis() - startTime;

            assertThat(elapsedTime).isGreaterThanOrEqualTo(400L);
            verify(uploader, times(2)).upload(any(byte[].class), any(Map.class));
        }
    }
}
