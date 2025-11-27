package com.example.ratingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    public static final String RATINGS_TOPIC = "ratings";
    
    @Bean
    public NewTopic ratingsTopic() {
        return TopicBuilder.name(RATINGS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
