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

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatResponse(String message) {
        String apiUrl = "https://openrouter.ai/api/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful recipe assistant."),
                Map.of("role", "user", "content", message)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://yourapp.com");
        headers.set("X-Title", "Recipe Explorer");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                return messageObj.get("content").toString();
            }

            return "⚠️ AI did not return a valid response.";

        } catch (Exception e) {
            System.err.println("⚠️ DeepSeek error: " + e.getMessage());
            return "⚠️ Something went wrong connecting to the AI. Please try again later.";
        }
    }
}
