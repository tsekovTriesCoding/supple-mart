package app.web;

import app.contact.dto.ContactRequest;
import app.contact.dto.ContactResponse;
import app.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact", description = "Contact form submission endpoint")
public class ContactController {
    
    private final ContactService contactService;
    
    @Operation(summary = "Submit contact form", description = "Submit a contact form message (public endpoint)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid form data")
    })
    @PostMapping
    public ResponseEntity<ContactResponse> submitContactForm(@Valid @RequestBody ContactRequest request) {
        log.info("Received contact form submission from: {}", request.getEmail());
        
        contactService.processContactRequest(request);
        
        ContactResponse response = ContactResponse.builder()
                .success(true)
                .message("Thank you for contacting us! We've received your message and will get back to you soon.")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
