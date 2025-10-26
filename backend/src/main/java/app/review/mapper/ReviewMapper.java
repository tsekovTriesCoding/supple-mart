package app.review.mapper;

import app.review.dto.ReviewDTO;
import app.review.model.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewDTO toDTO(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public List<ReviewDTO> toDTOList(List<Review> reviews) {
        return reviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
