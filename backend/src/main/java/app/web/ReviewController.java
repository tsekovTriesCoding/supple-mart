package app.web;

import app.review.dto.CreateReviewRequest;
import app.review.dto.UpdateReviewRequest;
import app.review.dto.Review;
import app.review.dto.ReviewResponse;
import app.review.service.ReviewService;
import app.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReviewResponse> reviews = reviewService.getUserReviewsDetailed(userDetails.getId());
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<Review> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateReviewRequest request) {
        Review updatedReview = reviewService.updateReview(id, userDetails.getId(), request);
        return ResponseEntity.ok(updatedReview);
    }
}
