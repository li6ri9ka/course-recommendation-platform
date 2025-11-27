package com.example.ratingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${user-service.url:http://user-service:8081}")
    private String userServiceUrl;
    
    @SuppressWarnings("unchecked")
    public Long getUserIdFromToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    userServiceUrl + "/api/users/validate",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("valid"))) {
                Object userId = response.getBody().get("userId");
                if (userId instanceof Integer) {
                    return ((Integer) userId).longValue();
                }
                return (Long) userId;
            }
        } catch (Exception e) {
            log.error("Failed to validate token with user service: {}", e.getMessage());
        }
        return null;
    }
}
