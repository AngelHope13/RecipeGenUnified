package com.recipegen.controller;

import com.recipegen.service.SmartRecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SmartRecipeController {

    private final SmartRecipeService smartRecipeService;

    public SmartRecipeController(SmartRecipeService smartRecipeService) {
        this.smartRecipeService = smartRecipeService;
    }

    /**
     * Chat endpoint for smart recipe generation based on user input.
     * Accepts message, area (nationality), and optional filters.
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        // Validate message input
        Object msgObj = request.get("message");
        if (!(msgObj instanceof String message) || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or missing message"));
        }

        // Area (optional, default to "")
        String area = (String) request.getOrDefault("area", "");

        // Filters (optional, cast safely)
        Map<String, Boolean> filters = null;
        Object filtersObj = request.get("filters");
        if (filtersObj instanceof Map<?, ?> map) {
            try {
                filters = map.entrySet().stream()
                        .filter(e -> e.getKey() instanceof String && e.getValue() instanceof Boolean)
                        .collect(Collectors.toMap(
                                e -> (String) e.getKey(),
                                e -> (Boolean) e.getValue()
                        ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid filters format"));
            }
        }

        // Delegate to service layer
        Map<String, Object> response = smartRecipeService.handleSmartChat(message, area, filters);
        return ResponseEntity.ok(response);
    }

    /**
     * Ingredient suggestion endpoint.
     * Supports real-time dropdown suggestions while typing.
     */
    @GetMapping("/suggestions")
    public List<String> suggest(@RequestParam String input) {
        return smartRecipeService.suggestIngredients(input);
    }
}
