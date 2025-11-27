package com.example.recommendationservice.service

import com.example.recommendationservice.dto.*
import com.example.recommendationservice.repository.RecommendationRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class RecommendationService(private val courseServiceUrl: String) {
    
    private val logger = LoggerFactory.getLogger(RecommendationService::class.java)
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    /**
     * Пересчитывает рекомендации для пользователя на основе:
     * 1. Категорий курсов, которые он высоко оценил
     * 2. Курсов, которые высоко оценили похожие пользователи (collaborative filtering)
     */
    suspend fun recalculateRecommendations(userId: Long) {
        logger.info("Recalculating recommendations for user: $userId")
        
        try {
            // Получаем оценки пользователя
            val userRatings = RecommendationRepository.getUserRatings(userId)
            if (userRatings.isEmpty()) {
                logger.info("User $userId has no ratings, generating default recommendations")
                generateDefaultRecommendations(userId)
                return
            }
            
            // Получаем высоко оценённые курсы (rating >= 4)
            val highRatedCourseIds = userRatings
                .filter { it.rating >= 4 }
                .map { it.courseId }
            
            // Получаем все оценки для collaborative filtering
            val allRatings = RecommendationRepository.getAllUserRatings()
            
            // Находим похожих пользователей (оценили те же курсы)
            val similarUsers = findSimilarUsers(userId, userRatings, allRatings)
            
            // Собираем рекомендации
            val recommendations = mutableListOf<CourseRecommendation>()
            
            // 1. Добавляем курсы от похожих пользователей
            val userRatedCourseIds = RecommendationRepository.getRatedCourseIds(userId)
            
            similarUsers.forEach { (similarUserId, similarity) ->
                val similarUserRatings = allRatings.filter { 
                    it.userId == similarUserId && it.rating >= 4 && it.courseId !in userRatedCourseIds 
                }
                
                similarUserRatings.forEach { rating ->
                    val existingRec = recommendations.find { it.courseId == rating.courseId }
                    if (existingRec == null) {
                        recommendations.add(
                            CourseRecommendation(
                                courseId = rating.courseId,
                                score = similarity * rating.rating / 5.0,
                                reason = "Рекомендовано на основе похожих пользователей"
                            )
                        )
                    }
                }
            }
            
            // Сортируем и сохраняем топ-10
            val topRecommendations = recommendations
                .sortedByDescending { it.score }
                .take(10)
            
            if (topRecommendations.isNotEmpty()) {
                RecommendationRepository.saveRecommendations(userId, topRecommendations)
                logger.info("Saved ${topRecommendations.size} recommendations for user $userId")
            } else {
                generateDefaultRecommendations(userId)
            }
            
        } catch (e: Exception) {
            logger.error("Error recalculating recommendations for user $userId", e)
        }
    }
    
    /**
     * Находит похожих пользователей на основе пересечения оценённых курсов
     */
    private fun findSimilarUsers(
        userId: Long,
        userRatings: List<UserRatingData>,
        allRatings: List<UserRatingData>
    ): List<Pair<Long, Double>> {
        val userCourseIds = userRatings.map { it.courseId }.toSet()
        
        return allRatings
            .filter { it.userId != userId }
            .groupBy { it.userId }
            .mapNotNull { (otherUserId, otherRatings) ->
                val otherCourseIds = otherRatings.map { it.courseId }.toSet()
                val commonCourses = userCourseIds.intersect(otherCourseIds)
                
                if (commonCourses.size >= 2) {
                    // Jaccard similarity
                    val union = userCourseIds.union(otherCourseIds)
                    val similarity = commonCourses.size.toDouble() / union.size
                    Pair(otherUserId, similarity)
                } else {
                    null
                }
            }
            .sortedByDescending { it.second }
            .take(10)
    }
    
    /**
     * Генерирует дефолтные рекомендации (топ курсы по рейтингу)
     */
    private suspend fun generateDefaultRecommendations(userId: Long) {
        try {
            val courses = fetchTopRatedCourses()
            val userRatedCourseIds = RecommendationRepository.getRatedCourseIds(userId)
            
            val recommendations = courses
                .filter { it.id !in userRatedCourseIds }
                .take(10)
                .mapIndexed { index, course ->
                    CourseRecommendation(
                        courseId = course.id,
                        score = 1.0 - (index * 0.05),
                        reason = "Популярный курс с высоким рейтингом"
                    )
                }
            
            if (recommendations.isNotEmpty()) {
                RecommendationRepository.saveRecommendations(userId, recommendations)
            }
        } catch (e: Exception) {
            logger.error("Error generating default recommendations", e)
        }
    }
    
    suspend fun getRecommendations(userId: Long): RecommendationResponse {
        val recommendations = RecommendationRepository.getRecommendations(userId)
        
        // Получаем информацию о курсах
        val enrichedRecommendations = if (recommendations.isNotEmpty()) {
            try {
                val courseIds = recommendations.map { it.courseId }
                val courses = fetchCoursesByIds(courseIds)
                val coursesMap = courses.associateBy { it.id }
                
                recommendations.map { rec ->
                    rec.copy(course = coursesMap[rec.courseId])
                }
            } catch (e: Exception) {
                logger.error("Error fetching course details", e)
                recommendations
            }
        } else {
            recommendations
        }
        
        return RecommendationResponse(userId, enrichedRecommendations)
    }
    
    private suspend fun fetchCoursesByIds(ids: List<Long>): List<CourseInfo> {
        return try {
            httpClient.post("$courseServiceUrl/api/courses/by-ids") {
                setBody(ids)
                header("Content-Type", "application/json")
            }.body()
        } catch (e: Exception) {
            logger.error("Error fetching courses by ids", e)
            emptyList()
        }
    }
    
    private suspend fun fetchTopRatedCourses(): List<CourseInfo> {
        return try {
            httpClient.get("$courseServiceUrl/api/courses/top-rated").body()
        } catch (e: Exception) {
            logger.error("Error fetching top rated courses", e)
            emptyList()
        }
    }
}
