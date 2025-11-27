package com.example.ratingservice.service;

import com.example.ratingservice.dto.*;
import com.example.ratingservice.entity.Rating;
import com.example.ratingservice.kafka.RatingEventProducer;
import com.example.ratingservice.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final RatingEventProducer ratingEventProducer;
    
    @Transactional
    public RatingResponse createOrUpdateRating(Long userId, CreateRatingRequest request) {
        Rating rating = ratingRepository.findByUserIdAndCourseId(userId, request.getCourseId())
                .orElse(null);
        
        String eventType;
        
        if (rating != null) {
            // Update existing rating
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());
            eventType = "UPDATED";
        } else {
            // Create new rating
            rating = Rating.builder()
                    .userId(userId)
                    .courseId(request.getCourseId())
                    .rating(request.getRating())
                    .review(request.getReview())
                    .build();
            eventType = "CREATED";
        }
        
        rating = ratingRepository.save(rating);
        
        // Publish event to Kafka
        RatingEvent event = RatingEvent.builder()
                .eventType(eventType)
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .courseId(rating.getCourseId())
                .rating(rating.getRating())
                .review(rating.getReview())
                .timestamp(LocalDateTime.now())
                .build();
        
        ratingEventProducer.sendRatingEvent(event);
        log.info("Rating {} event published for user {} and course {}", 
                eventType, userId, request.getCourseId());
        
        return RatingResponse.fromEntity(rating);
    }
    
    public RatingResponse getRatingById(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rating not found with id: " + id));
        return RatingResponse.fromEntity(rating);
    }
    
    public List<RatingResponse> getRatingsByUserId(Long userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(RatingResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<RatingResponse> getRatingsByCourseId(Long courseId) {
        return ratingRepository.findByCourseId(courseId).stream()
                .map(RatingResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public RatingResponse getUserRatingForCourse(Long userId, Long courseId) {
        Rating rating = ratingRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Rating not found for user " + userId + " and course " + courseId));
        return RatingResponse.fromEntity(rating);
    }
    
    public CourseRatingStats getCourseRatingStats(Long courseId) {
        Double averageRating = ratingRepository.getAverageRatingByCourseId(courseId);
        Integer totalRatings = ratingRepository.getTotalRatingsByCourseId(courseId);
        
        return CourseRatingStats.builder()
                .courseId(courseId)
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : null)
                .totalRatings(totalRatings != null ? totalRatings : 0)
                .build();
    }
    
    @Transactional
    public void deleteRating(Long userId, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found with id: " + ratingId));
        
        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own ratings");
        }
        
        ratingRepository.delete(rating);
        
        // Publish delete event to Kafka
        RatingEvent event = RatingEvent.builder()
                .eventType("DELETED")
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .courseId(rating.getCourseId())
                .rating(rating.getRating())
                .timestamp(LocalDateTime.now())
                .build();
        
        ratingEventProducer.sendRatingEvent(event);
        log.info("Rating DELETE event published for rating {}", ratingId);
    }
}
