package com.example.recommendationservice.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Recommendations : Table("recommendations") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val courseId = long("course_id")
    val score = double("score")
    val reason = varchar("reason", 500).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(userId, courseId)
    }
}
