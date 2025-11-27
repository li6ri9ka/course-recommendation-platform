package com.example.courseservice.repository;

import com.example.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByCategory(String category);
    
    List<Course> findByLevel(Course.Level level);
    
    List<Course> findByPublishedTrue();
    
    List<Course> findByCategoryAndPublishedTrue(String category);
    
    @Query("SELECT c FROM Course c WHERE c.published = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.published = true")
    List<String> findAllCategories();
    
    List<Course> findByIdIn(List<Long> ids);
    
    @Query("SELECT c FROM Course c WHERE c.published = true ORDER BY c.averageRating DESC NULLS LAST")
    List<Course> findTopRatedCourses();
}
