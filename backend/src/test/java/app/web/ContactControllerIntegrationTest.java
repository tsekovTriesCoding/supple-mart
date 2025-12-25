package app.web;

import app.BaseIntegrationTest;
import app.contact.dto.ContactRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ContactController.
 * Tests contact form submission endpoint with a real database using Testcontainers.
 * Note: Email sending is mocked in tests via TestMailConfig.
 */
@DisplayName("Contact Controller Integration Tests")
class ContactControllerIntegrationTest extends BaseIntegrationTest {

    private static final String CONTACT_BASE_URL = "/api/contact";

    @Nested
    @DisplayName("POST /api/contact")
    class SubmitContactFormTests {

        @Test
        @DisplayName("Should submit contact form successfully")
        void submitContactForm_ValidData_ReturnsCreated() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .subject("Question about products")
                    .message("I have a question about your protein supplements. Can you provide more information?")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 400 for missing name")
        void submitContactForm_MissingName_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .email("john.doe@example.com")
                    .subject("Question about products")
                    .message("I have a question about your protein supplements.")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing email")
        void submitContactForm_MissingEmail_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .subject("Question about products")
                    .message("I have a question about your protein supplements.")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void submitContactForm_InvalidEmail_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("not-a-valid-email")
                    .subject("Question about products")
                    .message("I have a question about your protein supplements.")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing subject")
        void submitContactForm_MissingSubject_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .message("I have a question about your protein supplements.")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for subject too short")
        void submitContactForm_SubjectTooShort_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .subject("Hi") // Less than 5 characters
                    .message("I have a question about your protein supplements.")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing message")
        void submitContactForm_MissingMessage_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .subject("Question about products")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for message too short")
        void submitContactForm_MessageTooShort_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .subject("Question about products")
                    .message("Too short") // Less than 10 characters
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for name too short")
        void submitContactForm_NameTooShort_ReturnsBadRequest() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("J") // Less than 2 characters
                    .email("john.doe@example.com")
                    .subject("Question about products")
                    .message("I have a question about your protein supplements.")
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept contact form without authentication (public endpoint)")
        void submitContactForm_NoAuth_StillWorks() throws Exception {
            ContactRequest request = ContactRequest.builder()
                    .name("Jane Doe")
                    .email("jane.doe@example.com")
                    .subject("Product inquiry")
                    .message("I would like to know more about your product range and shipping options.")
                    .build();

            // This endpoint should be public - no authentication required
            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should handle long valid message")
        void submitContactForm_LongMessage_ReturnsCreated() throws Exception {
            String longMessage = "This is a detailed message about my experience. ".repeat(20);

            ContactRequest request = ContactRequest.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .subject("Detailed feedback")
                    .message(longMessage)
                    .build();

            mockMvc.perform(post(CONTACT_BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
