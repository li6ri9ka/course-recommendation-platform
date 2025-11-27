package com.example.recommendationservice.config

import com.example.recommendationservice.entity.UserRatings
import com.example.recommendationservice.entity.Recommendations
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = environment.config
    
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.property("database.url").getString()
        driverClassName = config.property("database.driver").getString()
        username = config.property("database.user").getString()
        password = config.property("database.password").getString()
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    
    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)
    
    transaction {
        SchemaUtils.create(UserRatings, Recommendations)
    }
    
    log.info("Database connected successfully")
}
