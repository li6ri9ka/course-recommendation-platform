package com.example.recommendationservice.routes

import com.example.recommendationservice.dto.ErrorResponse
import com.example.recommendationservice.dto.RecommendationResponse
import com.example.recommendationservice.repository.RecommendationRepository
import com.example.recommendationservice.service.RecommendationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.recommendationRoutes() {
    val courseServiceUrl = application.environment.config
        .property("services.courseServiceUrl").getString()
    val recommendationService = RecommendationService(courseServiceUrl)
    
    route("/api/recommendations") {
        
        // GET /api/recommendations/{userId}
        get("/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
                return@get
            }
            
            val recommendations = recommendationService.getRecommendations(userId)
            call.respond(HttpStatusCode.OK, recommendations)
        }
        
        // POST /api/recommendations/{userId}/recalculate
        post("/{userId}/recalculate") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
                return@post
            }
            
            recommendationService.recalculateRecommendations(userId)
            call.respond(HttpStatusCode.OK, mapOf("message" to "Recommendations recalculated"))
        }
        
        // GET /api/recommendations/{userId}/ratings
        get("/{userId}/ratings") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
                return@get
            }
            
            val ratings = RecommendationRepository.getUserRatings(userId)
            call.respond(HttpStatusCode.OK, ratings)
        }
    }
    
    // Health check
    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
    }
}
