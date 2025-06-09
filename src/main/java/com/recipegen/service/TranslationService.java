package com.recipegen.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔄 You can swap this with your local LibreTranslate container later if needed
    private static final String TRANSLATE_URL = "https://libretranslate.de/translate";

    public String translateToEnglish(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("q", input);
        payload.put("source", "auto");
        payload.put("target", "en");
        payload.put("format", "text");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(TRANSLATE_URL, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object translated = response.getBody().get("translatedText");
                if (translated != null) {
                    System.out.println("🔤 Translated to English: " + translated);
                    return translated.toString();
                } else {
                    System.err.println("⚠️ Missing 'translatedText' in response.");
                }
            } else {
                System.err.println("⚠️ Translation API returned non-OK status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Translation error: " + e.getMessage());
        }

        // Fallback: original input
        return input;
    }
}
