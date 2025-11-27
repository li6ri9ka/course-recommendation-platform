package com.example.recommendationservice.repository

import com.example.recommendationservice.dto.CourseRecommendation
import com.example.recommendationservice.dto.UserRatingData
import com.example.recommendationservice.entity.Recommendations
import com.example.recommendationservice.entity.UserRatings
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object RecommendationRepository {
    
    // User Ratings operations
    fun saveOrUpdateUserRating(userId: Long, courseId: Long, rating: Int) {
        transaction {
            val existing = UserRatings.select {
                (UserRatings.userId eq userId) and (UserRatings.courseId eq courseId)
            }.singleOrNull()
            
            if (existing != null) {
                UserRatings.update({
                    (UserRatings.userId eq userId) and (UserRatings.courseId eq courseId)
                }) {
                    it[UserRatings.rating] = rating
                    it[updatedAt] = LocalDateTime.now()
                }
            } else {
                UserRatings.insert {
                    it[UserRatings.userId] = userId
                    it[UserRatings.courseId] = courseId
                    it[UserRatings.rating] = rating
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }
            }
        }
    }
    
    fun deleteUserRating(userId: Long, courseId: Long) {
        transaction {
            UserRatings.deleteWhere {
                (UserRatings.userId eq userId) and (UserRatings.courseId eq courseId)
            }
        }
    }
    
    fun getUserRatings(userId: Long): List<UserRatingData> {
        return transaction {
            UserRatings.select { UserRatings.userId eq userId }
                .map {
                    UserRatingData(
                        userId = it[UserRatings.userId],
                        courseId = it[UserRatings.courseId],
                        rating = it[UserRatings.rating]
                    )
                }
        }
    }
    
    fun getAllUserRatings(): List<UserRatingData> {
        return transaction {
            UserRatings.selectAll()
                .map {
                    UserRatingData(
                        userId = it[UserRatings.userId],
                        courseId = it[UserRatings.courseId],
                        rating = it[UserRatings.rating]
                    )
                }
        }
    }
    
    fun getUsersWhoRatedCourse(courseId: Long): List<Long> {
        return transaction {
            UserRatings.select { UserRatings.courseId eq courseId }
                .map { it[UserRatings.userId] }
        }
    }
    
    // Recommendations operations
    fun saveRecommendations(userId: Long, recommendations: List<CourseRecommendation>) {
        transaction {
            // Delete old recommendations for user
            Recommendations.deleteWhere { Recommendations.userId eq userId }
            
            // Insert new recommendations
            recommendations.forEach { rec ->
                Recommendations.insert {
                    it[Recommendations.userId] = userId
                    it[courseId] = rec.courseId
                    it[score] = rec.score
                    it[reason] = rec.reason
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }
            }
        }
    }
    
    fun getRecommendations(userId: Long): List<CourseRecommendation> {
        return transaction {
            Recommendations.select { Recommendations.userId eq userId }
                .orderBy(Recommendations.score, SortOrder.DESC)
                .limit(10)
                .map {
                    CourseRecommendation(
                        courseId = it[Recommendations.courseId],
                        score = it[Recommendations.score],
                        reason = it[Recommendations.reason]
                    )
                }
        }
    }
    
    fun getRatedCourseIds(userId: Long): Set<Long> {
        return transaction {
            UserRatings.select { UserRatings.userId eq userId }
                .map { it[UserRatings.courseId] }
                .toSet()
        }
    }
}
