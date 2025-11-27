package com.example.recommendationservice

import com.example.recommendationservice.config.configureDatabase
import com.example.recommendationservice.config.configureRouting
import com.example.recommendationservice.config.configureSecurity
import com.example.recommendationservice.config.configureSerialization
import com.example.recommendationservice.kafka.RatingEventConsumer
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureSecurity()
    configureRouting()
    
    // Start Kafka consumer
    val consumer = RatingEventConsumer(this)
    launch {
        consumer.start()
    }
    
    environment.monitor.subscribe(ApplicationStopped) {
        consumer.stop()
    }
}
