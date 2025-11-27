package com.example.recommendationservice.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingEvent(
    val eventType: String,
    val ratingId: Long,
    val userId: Long,
    val courseId: Long,
    val rating: Int,
    val review: String? = null,
    val timestamp: String
)

@Serializable
data class RecommendationResponse(
    val userId: Long,
    val recommendations: List<CourseRecommendation>
)

@Serializable
data class CourseRecommendation(
    val courseId: Long,
    val score: Double,
    val reason: String?,
    val course: CourseInfo? = null
)

@Serializable
data class CourseInfo(
    val id: Long,
    val title: String,
    val description: String? = null,
    val category: String,
    val instructorName: String? = null,
    val level: String,
    val averageRating: Double? = null,
    val totalRatings: Int? = null
)

@Serializable
data class UserRatingData(
    val userId: Long,
    val courseId: Long,
    val rating: Int
)

@Serializable
data class ErrorResponse(
    val error: String
)
