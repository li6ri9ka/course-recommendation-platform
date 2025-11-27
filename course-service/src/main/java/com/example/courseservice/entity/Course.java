package com.example.courseservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "instructor_name")
    private String instructorName;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "duration_hours")
    private Integer durationHours;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level = Level.BEGINNER;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(nullable = false)
    private Boolean published = false;
    
    @Column(name = "average_rating")
    private Double averageRating;
    
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Level {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
