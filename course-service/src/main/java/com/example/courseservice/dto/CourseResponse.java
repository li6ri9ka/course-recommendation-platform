package com.example.courseservice.dto;

import com.example.courseservice.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    
    private Long id;
    private String title;
    private String description;
    private String category;
    private String instructorName;
    private BigDecimal price;
    private Integer durationHours;
    private String level;
    private String imageUrl;
    private Boolean published;
    private Double averageRating;
    private Integer totalRatings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CourseResponse fromEntity(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .instructorName(course.getInstructorName())
                .price(course.getPrice())
                .durationHours(course.getDurationHours())
                .level(course.getLevel().name())
                .imageUrl(course.getImageUrl())
                .published(course.getPublished())
                .averageRating(course.getAverageRating())
                .totalRatings(course.getTotalRatings())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
