package com.example.ratingservice.controller;

import com.example.ratingservice.dto.CourseRatingStats;
import com.example.ratingservice.dto.CreateRatingRequest;
import com.example.ratingservice.dto.RatingResponse;
import com.example.ratingservice.service.RatingService;
import com.example.ratingservice.service.UserServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    
    private final RatingService ratingService;
    private final UserServiceClient userServiceClient;
    
    @PostMapping
    public ResponseEntity<RatingResponse> createOrUpdateRating(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateRatingRequest request) {
        
        String token = authHeader.replace("Bearer ", "");
        Long userId = userServiceClient.getUserIdFromToken(token);
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.createOrUpdateRating(userId, request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RatingResponse> getRatingById(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingById(id));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingResponse>> getRatingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getRatingsByUserId(userId));
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<RatingResponse>> getRatingsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.getRatingsByCourseId(courseId));
    }
    
    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<RatingResponse> getUserRatingForCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.getUserRatingForCourse(userId, courseId));
    }
    
    @GetMapping("/stats/{courseId}")
    public ResponseEntity<CourseRatingStats> getCourseRatingStats(@PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.getCourseRatingStats(courseId));
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<RatingResponse>> getMyRatings(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        Long userId = userServiceClient.getUserIdFromToken(token);
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return ResponseEntity.ok(ratingService.getRatingsByUserId(userId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        
        String token = authHeader.replace("Bearer ", "");
        Long userId = userServiceClient.getUserIdFromToken(token);
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        ratingService.deleteRating(userId, id);
        return ResponseEntity.noContent().build();
    }
}
