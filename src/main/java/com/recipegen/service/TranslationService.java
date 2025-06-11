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
}
