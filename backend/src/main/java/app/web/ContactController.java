package app.web;

import app.contact.dto.ContactRequest;
import app.contact.dto.ContactResponse;
import app.contact.service.ContactService;
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
public class ContactController {
    
    private final ContactService contactService;
    
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
