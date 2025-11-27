package com.example.recommendationservice.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UserRatings : Table("user_ratings") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val courseId = long("course_id")
    val rating = integer("rating")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(userId, courseId)
    }
}
