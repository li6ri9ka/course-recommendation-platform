package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseResponse;
import com.example.courseservice.dto.CreateCourseRequest;
import com.example.courseservice.dto.UpdateCourseRequest;
import com.example.courseservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }
    
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses(
            @RequestParam(required = false) Boolean published) {
        if (Boolean.TRUE.equals(published)) {
            return ResponseEntity.ok(courseService.getPublishedCourses());
        }
        return ResponseEntity.ok(courseService.getAllCourses());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseResponse>> getCoursesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(category));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(courseService.getAllCategories());
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<CourseResponse>> getTopRatedCourses() {
        return ResponseEntity.ok(courseService.getTopRatedCourses());
    }
    
    @PostMapping("/by-ids")
    public ResponseEntity<List<CourseResponse>> getCoursesByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(courseService.getCoursesByIds(ids));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }
    
    @PutMapping("/{id}/rating")
    public ResponseEntity<Void> updateCourseRating(
            @PathVariable Long id,
            @RequestParam Double averageRating,
            @RequestParam Integer totalRatings) {
        courseService.updateCourseRating(id, averageRating, totalRatings);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
