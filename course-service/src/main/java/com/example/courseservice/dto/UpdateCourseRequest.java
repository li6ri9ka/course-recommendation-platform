package com.example.courseservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {
    
    private String title;
    
    private String description;
    
    private String category;
    
    private String instructorName;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Positive(message = "Duration must be positive")
    private Integer durationHours;
    
    private String level;
    
    private String imageUrl;
    
    private Boolean published;
}
