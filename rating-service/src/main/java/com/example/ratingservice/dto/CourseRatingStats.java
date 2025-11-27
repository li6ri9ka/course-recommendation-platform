package com.example.ratingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingStats {
    
    private Long courseId;
    private Double averageRating;
    private Integer totalRatings;
}
