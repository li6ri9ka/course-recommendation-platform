package com.example.recommendationservice.kafka

import com.example.recommendationservice.dto.RatingEvent
import com.example.recommendationservice.repository.RecommendationRepository
import com.example.recommendationservice.service.RecommendationService
import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

class RatingEventConsumer(application: Application) {
    
    private val logger = LoggerFactory.getLogger(RatingEventConsumer::class.java)
    private val config = application.environment.config
    
    private val bootstrapServers = config.property("kafka.bootstrapServers").getString()
    private val groupId = config.property("kafka.groupId").getString()
    private val topic = config.property("kafka.topic").getString()
    private val courseServiceUrl = config.property("services.courseServiceUrl").getString()
    
    private val recommendationService = RecommendationService(courseServiceUrl)
    
    private var consumer: KafkaConsumer<String, String>? = null
    private var running = true
    private var consumerThread: Thread? = null
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    fun start() {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        }
        
        consumerThread = thread(start = true, name = "kafka-consumer") {
            try {
                consumer = KafkaConsumer<String, String>(props)
                consumer?.subscribe(listOf(topic))
                
                logger.info("Kafka consumer started, listening to topic: $topic")
                
                while (running) {
                    try {
                        val records = consumer?.poll(Duration.ofMillis(1000))
                        
                        records?.forEach { record ->
                            try {
                                logger.debug("Received message: ${record.value()}")
                                val event = json.decodeFromString<RatingEvent>(record.value())
                                processRatingEvent(event)
                            } catch (e: Exception) {
                                logger.error("Error processing message: ${record.value()}", e)
                            }
                        }
                    } catch (e: Exception) {
                        if (running) {
                            logger.error("Error polling messages", e)
                            Thread.sleep(1000)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Kafka consumer error", e)
            } finally {
                consumer?.close()
                logger.info("Kafka consumer stopped")
            }
        }
    }
    
    private fun processRatingEvent(event: RatingEvent) {
        logger.info("Processing rating event: type=${event.eventType}, userId=${event.userId}, courseId=${event.courseId}")
        
        when (event.eventType) {
            "CREATED", "UPDATED" -> {
                RecommendationRepository.saveOrUpdateUserRating(
                    event.userId,
                    event.courseId,
                    event.rating
                )
                
                // Пересчитываем рекомендации асинхронно
                CoroutineScope(Dispatchers.IO).launch {
                    recommendationService.recalculateRecommendations(event.userId)
                }
            }
            "DELETED" -> {
                RecommendationRepository.deleteUserRating(event.userId, event.courseId)
                
                CoroutineScope(Dispatchers.IO).launch {
                    recommendationService.recalculateRecommendations(event.userId)
                }
            }
            else -> {
                logger.warn("Unknown event type: ${event.eventType}")
            }
        }
    }
    
    fun stop() {
        running = false
        consumer?.wakeup()
        consumerThread?.join(5000)
    }
}
