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

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatResponse(String userMessage) {
        String url = GEMINI_API_URL + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🔐 Gemini Prompt: enforce safety and formatting
        String prompt = """
            You are a cooking assistant embedded in a recipe site that already shows full recipes.

            🚫 DO NOT:
            - Suggest dish names
            - Provide cooking instructions or steps
            - Mention any full recipe title

            ✅ INSTEAD:
            - Share ingredient storage tips
            - Recommend substitutions or nutritional hacks
            - Suggest flavor combos or prep techniques

            📋 Format:
            <strong>Cooking Tip:</strong><br>
            Start your message with this and use <br> for line breaks.
            Add a short <em>follow-up question</em> at the end.

            Now provide one short piece of advice for: %s
            """.formatted(userMessage);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            System.out.println("🔍 Gemini API response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> first = candidates.get(0);

                    // Preferred response structure
                    Map<String, Object> content = (Map<String, Object>) first.get("content");
                    if (content != null && content.get("parts") instanceof List<?> parts) {
                        Map<String, Object> part = (Map<String, Object>) parts.get(0);
                        if (part != null && part.get("text") != null) {
                            return part.get("text").toString();
                        }
                    }

                    // Fallback: top-level text
                    if (first.get("text") != null) {
                        return first.get("text").toString();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Gemini API error: " + e.getMessage());
        }

        // Default fallback response
        return "<strong>Cooking Tip:</strong><br>Store herbs with damp paper towels to keep them fresh longer.<br><em>Want more herb storage tips?</em>";
    }
}
