package com.recipegen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TranslationService {

    @Value("${google.translate.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GOOGLE_TRANSLATE_URL =
            "https://translation.googleapis.com/language/translate/v2";

    public String translateToEnglish(String input) {
        return translate(input, "en");
    }

    public String translate(String input, String targetLang) {
        if (input == null || input.trim().isEmpty()) return input;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> data = new HashMap<>();
        data.put("q", input);
        data.put("target", targetLang);
        data.put("format", "text");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

        try {
            String url = GOOGLE_TRANSLATE_URL + "?key=" + apiKey;

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> translations =
                        (List<Map<String, Object>>)
                                ((Map<String, Object>) response.getBody().get("data")).get("translations");

                if (translations != null && !translations.isEmpty()) {
                    return (String) translations.get(0).get("translatedText");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Google Translate API error: " + e.getMessage());
        }

        return input; // fallback
    }

    /**
     * Translate from English to the specified language.
     * If lang is null or "en", it returns the original English.
     */
    public String translateFromEnglish(String englishText, String targetLang) {
        if (targetLang == null || targetLang.equalsIgnoreCase("en")) {
            return englishText;
        }
        return translate(englishText, targetLang);
    }

    /**
     * Detect the language of a given input string.
     * Defaults to "en" if detection fails.
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) return "en";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> data = new HashMap<>();
        data.put("q", List.of(text));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

        try {
            String url = GOOGLE_TRANSLATE_URL + "/detect?key=" + apiKey;

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> detections =
                        (List<Map<String, Object>>) ((Map<String, Object>) response.getBody().get("data")).get("detections");

                if (detections != null && !detections.isEmpty() && detections.get(0) instanceof List) {
                    Map<String, Object> detection = (Map<String, Object>) ((List<?>) detections.get(0)).get(0);
                    return (String) detection.get("language");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Language detection error: " + e.getMessage());
        }

        return "en"; // fallback
    }
}
