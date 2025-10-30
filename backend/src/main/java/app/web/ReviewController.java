package app.web;

import app.review.dto.CreateReviewRequest;
import app.review.dto.ReviewDTO;
import app.review.dto.ReviewResponseDTO;
import app.review.service.ReviewService;
import app.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getUserReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReviewResponseDTO> reviews = reviewService.getUserReviewsDetailed(userDetails.getId());
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO review = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

}
