package com.example.ratingservice.dto;

import com.example.ratingservice.entity.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    
    private Long id;
    private Long userId;
    private Long courseId;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RatingResponse fromEntity(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUserId())
                .courseId(rating.getCourseId())
                .rating(rating.getRating())
                .review(rating.getReview())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
