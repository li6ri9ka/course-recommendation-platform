package com.example.courseservice.service;

import com.example.courseservice.dto.CourseResponse;
import com.example.courseservice.dto.CreateCourseRequest;
import com.example.courseservice.dto.UpdateCourseRequest;
import com.example.courseservice.entity.Course;
import com.example.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .instructorName(request.getInstructorName())
                .price(request.getPrice())
                .durationHours(request.getDurationHours())
                .level(request.getLevel() != null ? 
                        Course.Level.valueOf(request.getLevel().toUpperCase()) : 
                        Course.Level.BEGINNER)
                .imageUrl(request.getImageUrl())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .totalRatings(0)
                .build();
        
        course = courseRepository.save(course);
        return CourseResponse.fromEntity(course);
    }
    
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return CourseResponse.fromEntity(course);
    }
    
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getPublishedCourses() {
        return courseRepository.findByPublishedTrue().stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getCoursesByCategory(String category) {
        return courseRepository.findByCategoryAndPublishedTrue(category).stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.searchByKeyword(keyword).stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<String> getAllCategories() {
        return courseRepository.findAllCategories();
    }
    
    public List<CourseResponse> getCoursesByIds(List<Long> ids) {
        return courseRepository.findByIdIn(ids).stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getTopRatedCourses() {
        return courseRepository.findTopRatedCourses().stream()
                .limit(10)
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            course.setCategory(request.getCategory());
        }
        if (request.getInstructorName() != null) {
            course.setInstructorName(request.getInstructorName());
        }
        if (request.getPrice() != null) {
            course.setPrice(request.getPrice());
        }
        if (request.getDurationHours() != null) {
            course.setDurationHours(request.getDurationHours());
        }
        if (request.getLevel() != null) {
            course.setLevel(Course.Level.valueOf(request.getLevel().toUpperCase()));
        }
        if (request.getImageUrl() != null) {
            course.setImageUrl(request.getImageUrl());
        }
        if (request.getPublished() != null) {
            course.setPublished(request.getPublished());
        }
        
        course = courseRepository.save(course);
        return CourseResponse.fromEntity(course);
    }
    
    @Transactional
    public void updateCourseRating(Long courseId, Double averageRating, Integer totalRatings) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        course.setAverageRating(averageRating);
        course.setTotalRatings(totalRatings);
        courseRepository.save(course);
    }
    
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
}
