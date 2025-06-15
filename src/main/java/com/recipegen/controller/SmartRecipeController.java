package com.recipegen.controller;

import com.recipegen.service.ChatService;
import com.recipegen.service.SmartRecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SmartRecipeController {

    private final SmartRecipeService smartRecipeService;
    private final ChatService chatService;

    public SmartRecipeController(SmartRecipeService smartRecipeService, ChatService chatService) {
        this.smartRecipeService = smartRecipeService;
        this.chatService = chatService;
    }

    /**
     * POST /chat — Accepts a message and returns recipes + a cooking tip.
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            Object msgObj = request.get("message");
            if (!(msgObj instanceof String message) || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or missing message"));
            }

            String area = (String) request.getOrDefault("area", "");
            String lang = (String) request.getOrDefault("lang", "en");

            Map<String, Boolean> filters = Collections.emptyMap();
            Object filtersObj = request.get("filters");

            if (filtersObj instanceof Map<?, ?> map) {
                filters = new HashMap<>(map.entrySet().stream()
                        .filter(e -> e.getKey() instanceof String && e.getValue() instanceof Boolean)
                        .collect(Collectors.toMap(
                                e -> (String) e.getKey(),
                                e -> (Boolean) e.getValue()
                        )));
            }

            System.out.println("📩 Request received | message=\"" + message + "\", area=" + area + ", filters=" + filters + ", lang=" + lang);

            Map<String, Object> recipeResponse = smartRecipeService.handleSmartChat(message, area, filters, lang);
            return ResponseEntity.ok(recipeResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Unexpected server error: " + e.getMessage()));
        }
    }

    /**
     * GET /suggestions — Returns ingredient suggestions based on partial input.
     */
    @GetMapping("/suggestions")
    public List<String> suggest(@RequestParam String input) {
        return smartRecipeService.suggestIngredients(input);
    }
}
