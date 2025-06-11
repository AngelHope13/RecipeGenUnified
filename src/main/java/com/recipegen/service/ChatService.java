package com.recipegen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // ✅ Corrected Gemini model endpoint
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatResponse(String userMessage) {
        String url = GEMINI_API_URL + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", userMessage)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            System.out.println("🔍 Gemini raw response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> first = candidates.get(0);

                    // ✅ Extract from: content → parts → text
                    if (first.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) first.get("content");
                        if (content != null && content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                                return parts.get(0).get("text").toString();
                            }
                        }
                    }

                    // 🟨 Optional fallback
                    if (first.containsKey("text")) {
                        return first.get("text").toString();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Gemini API error: " + e.getMessage());
        }

        return "Sorry, I couldn't generate a recipe right now.";
    }
}
