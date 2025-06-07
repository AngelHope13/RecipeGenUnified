package com.recipegen.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String translateToEnglish(String input) {
        String url = "https://libretranslate.de/translate";

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("q", input);
        requestBody.put("source", "auto");
        requestBody.put("target", "en");
        requestBody.put("format", "text");

        // Wrap into HttpEntity
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Make POST request
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null && result.containsKey("translatedText")) {
                return result.get("translatedText").toString();
            } else {
                System.err.println("Translation API returned no translation.");
                return input;
            }

        } catch (Exception e) {
            System.err.println("⚠️ Translation error: " + e.getMessage());
            return input; // Fallback: return original input
        }
    }
}
