package app.web;

import app.review.dto.CreateReviewRequest;
import app.review.dto.UpdateReviewRequest;
import app.review.dto.Review;
import app.review.dto.ReviewResponse;
import app.review.service.ReviewService;
import app.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reviews", description = "Product review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get user reviews", description = "Retrieve all reviews submitted by the current user")
    @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully")
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReviewResponse> reviews = reviewService.getUserReviewsDetailed(userDetails.getId());
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Create review", description = "Submit a new review for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully",
                    content = @Content(schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "400", description = "Invalid review data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ResponseEntity<Review> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @Operation(summary = "Delete review", description = "Delete a review (only own reviews can be deleted)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update review", description = "Update an existing review (only own reviews can be updated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @Parameter(description = "Review ID") @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateReviewRequest request) {
        Review updatedReview = reviewService.updateReview(id, userDetails.getId(), request);
        return ResponseEntity.ok(updatedReview);
    }
}
