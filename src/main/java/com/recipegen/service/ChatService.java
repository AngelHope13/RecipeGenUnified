package com.recipegen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public String getChatResponse(String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // Request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are a friendly cooking assistant."));
            messages.add(Map.of("role", "user", "content", userMessage));

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Call OpenRouter (DeepSeek)
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, requestEntity, Map.class);

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return message.get("content").toString();
                }
            }

            return "⚠️ AI could not respond at this time.";
        } catch (Exception e) {
            System.err.println("🔌 DeepSeek API Error: " + e.getMessage());
            return "⚠️ Something went wrong connecting to the AI. Please try again later.";
        }
    }
}
