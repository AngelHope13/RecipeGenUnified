package com.recipegen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public String getRecipesByIngredientAndNationality(List<String> ingredients, String nationality) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "{\"meals\": []}";
        }

        List<Map<String, String>> meals = fetchMealsByIngredient(ingredients.get(0));
        if (meals == null || meals.isEmpty()) {
            return "{\"meals\": []}";
        }

        if (nationality == null || nationality.isEmpty()) {
            return toJson(meals);
        }

        List<Map<String, String>> areaMeals = fetchMealsByNationality(nationality);
        if (areaMeals == null || areaMeals.isEmpty()) {
            return "{\"meals\": []}";
        }

        Set<String> areaMealIds = areaMeals.stream()
                .map(m -> m.get("idMeal"))
                .collect(Collectors.toSet());

        List<Map<String, String>> filteredMeals = meals.stream()
                .filter(m -> areaMealIds.contains(m.get("idMeal")))
                .collect(Collectors.toList());

        return toJson(filteredMeals);
    }

    private List<Map<String, String>> fetchMealsByIngredient(String ingredient) {
        try {
            String url = "https://www.themealdb.com/api/json/v1/1/filter.php?i=" + ingredient;
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return (List<Map<String, String>>) result.get("meals");
        } catch (Exception e) {
            System.err.println("Error fetching meals by ingredient: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> fetchMealsByNationality(String nationality) {
        try {
            String url = "https://www.themealdb.com/api/json/v1/1/filter.php?a=" + nationality;
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return (List<Map<String, String>>) result.get("meals");
        } catch (Exception e) {
            System.err.println("Error fetching meals by nationality: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private String toJson(List<Map<String, String>> meals) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("meals", meals);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            System.err.println("Error converting meals to JSON: " + e.getMessage());
            return "{\"meals\": []}";
        }
    }
}
