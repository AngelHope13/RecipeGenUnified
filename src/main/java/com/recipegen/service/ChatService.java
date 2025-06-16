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

    private final RestTemplate restTemplate = new RestTemplate();
    private final TranslationService translationService;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    public ChatService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public String getChatResponse(String userInput) {
        // 🌐 Detect original language and translate to English for Gemini
        String originalLang = translationService.detectLanguage(userInput);
        String userMessage = translationService.translateToEnglish(userInput);

        // 🧠 Construct system-guided instruction as part of message
        String prompt = """
            You are a helpful, slightly playful cooking assistant named KitchenWhiz 🍳.
            The user just typed: "%s"

            🎯 IMPORTANT:
            - DO NOT suggest dishes, recipes, or cooking steps.
            - DO NOT repeat yourself.
            - DO NOT use <strong> or <em> in the reply. That will be added later.
            ✅ DO:
            - Respond with a fun food tip, a kitchen hack, or a substitution idea.
            - If their message is confusing, make a light joke and prompt for ingredients.

            Be brief, kind, and sound like you're chatting. End with a playful follow-up question.
            """.formatted(userMessage);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = GEMINI_API_URL + geminiApiKey;

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

                    if (content != null && content.get("parts") instanceof List<?> parts) {
                        Map<String, Object> part = (Map<String, Object>) parts.get(0);
                        if (part != null && part.get("text") != null) {
                            String clean = part.get("text").toString().trim();

                            // 🧹 Normalize output formatting (no double "Cooking Tip")
                            if (clean.toLowerCase().contains("cooking tip:")) {
                                clean = clean.replaceAll("(?i)cooking tip[:：]?\\s*", "").trim();
                            }

                            return "<strong>Cooking Tip:</strong><br>" + clean + "<br><em>Got anything else in your fridge?</em>";
                        }
                    }

                    // Fallback: legacy key
                    if (candidates.get(0).get("text") != null) {
                        return "<strong>Cooking Tip:</strong><br>" +
                                candidates.get(0).get("text").toString() +
                                "<br><em>Want another tip?</em>";
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Gemini API error: " + e.getMessage());
        }

        // 🔁 Default fallback
        return "<strong>Cooking Tip:</strong><br>Oops! I didn't quite catch that—maybe try typing a few ingredients like 🥦 broccoli or 🍗 chicken.<br><em>Got anything else in your fridge?</em>";
    }
}
