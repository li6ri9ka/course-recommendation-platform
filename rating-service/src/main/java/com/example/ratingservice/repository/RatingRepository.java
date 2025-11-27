package com.example.ratingservice.repository;

import com.example.ratingservice.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    List<Rating> findByUserId(Long userId);
    
    List<Rating> findByCourseId(Long courseId);
    
    Optional<Rating> findByUserIdAndCourseId(Long userId, Long courseId);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.courseId = :courseId")
    Integer getTotalRatingsByCourseId(@Param("courseId") Long courseId);
    
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
