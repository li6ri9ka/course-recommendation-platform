package com.example.ratingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingEvent {
    
    private String eventType; // CREATED, UPDATED, DELETED
    private Long ratingId;
    private Long userId;
    private Long courseId;
    private Integer rating;
    private String review;
    private LocalDateTime timestamp;
}
