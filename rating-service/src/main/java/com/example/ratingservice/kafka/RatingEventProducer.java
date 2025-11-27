package com.example.ratingservice.kafka;

import com.example.ratingservice.config.KafkaConfig;
import com.example.ratingservice.dto.RatingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingEventProducer {
    
    private final KafkaTemplate<String, RatingEvent> kafkaTemplate;
    
    public void sendRatingEvent(RatingEvent event) {
        String key = event.getUserId() + "-" + event.getCourseId();
        
        CompletableFuture<SendResult<String, RatingEvent>> future = 
                kafkaTemplate.send(KafkaConfig.RATINGS_TOPIC, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Rating event sent successfully: {} with offset: {}", 
                        event, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send rating event: {}", event, ex);
            }
        });
    }
}
