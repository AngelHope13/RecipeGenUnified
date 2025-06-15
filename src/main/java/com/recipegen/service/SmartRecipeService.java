package com.recipegen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmartRecipeService {

    private final TranslationService translationService;
    private final ChatService chatService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spoonacular.api.key}")
    private String spoonacularApiKey;

    private static final Map<String, String> ingredientSynonyms = Map.ofEntries(
            Map.entry("aubergine", "eggplant"),
            Map.entry("courgette", "zucchini"),
            Map.entry("capsicum", "bell pepper"),
            Map.entry("coriander", "cilantro"),
            Map.entry("maize", "corn"),
            Map.entry("mushrooms", "mushroom"),
            Map.entry("chilli", "chili"),
            Map.entry("garbanzo", "chickpea"),
            Map.entry("scallion", "green onion")
    );

    public SmartRecipeService(TranslationService translationService, ChatService chatService) {
        this.translationService = translationService;
        this.chatService = chatService;
    }

    public Map<String, Object> handleSmartChat(String message, String area, Map<String, Boolean> filters, String lang) {
        Map<String, Object> response = new HashMap<>();

        String english = translationService.translateToEnglish(message);
        List<String> knownIngredients = fetchAllIngredients();

        List<String> words = Arrays.stream(english.toLowerCase().split("[ ,.!?]+"))
                .map(w -> ingredientSynonyms.getOrDefault(w, w))
                .collect(Collectors.toList());

        List<String> matched = new ArrayList<>();
        for (String word : words) {
            for (String ingredient : knownIngredients) {
                if (ingredient.toLowerCase().contains(word)) {
                    matched.add(ingredient);
                    break;
                }
            }
        }

        // Fallback: if no ingredients API or no matched
        if (knownIngredients.isEmpty()) {
            matched = new ArrayList<>(words.stream().distinct().limit(5).toList());
        }

        if (matched.isEmpty()) {
            response.put("reply", "😥 Sorry, I couldn't recognize any ingredients in your message.");
            response.put("recipes", Collections.emptyList());
            return response;
        }

        Set<String> seenIds = new HashSet<>();
        List<Map<String, String>> meals = new ArrayList<>();

        // Fetch MealDB for each ingredient individually
        for (String ing : matched) {
            List<Map<String, String>> partialResults = fetchRecipes(ing, area);
            for (Map<String, String> m : partialResults) {
                if (seenIds.add(m.get("idMeal"))) {
                    meals.add(m);
                }
            }
        }

        // Spoonacular fallback
        List<Map<String, String>> spoonacularMeals = fetchSpoonacularRecipes(String.join(",", matched));
        for (Map<String, String> meal : spoonacularMeals) {
            if (!seenIds.contains(meal.get("idMeal"))) {
                meals.add(meal);
            }
        }

        meals = applyFilters(meals, filters);

        StringBuilder reply = new StringBuilder();
        reply.append(meals.isEmpty() ? "🔍 No full matches found. Displaying similar results below.\n\n"
                : "🍽 Recipes based on your ingredients are shown below.\n\n");

        String prompt = """
            The user mentioned: %s.
            DO NOT include recipes, dish names, steps, or ingredients.
            ✅ Instead, share one cooking tip, food myth, kitchen hack, or ingredient substitution.
            Format it like:
            <strong>Cooking Tip:</strong><br>Onions are sweeter when sautéed slowly.
            <em>Want to know why onions make you cry?</em>
            """.formatted(String.join(", ", matched));

        String aiReply = chatService.getChatResponse(prompt);
        if (aiReply.toLowerCase().matches(".*\\b(recipe|steps?|boil|bake|fry|chop|mix|combine)\\b.*")) {
            aiReply = "<strong>Cooking Tip:</strong><br>Use fresh herbs last for the best aroma.<br><em>Want to learn how to store them longer?</em>";
        }

        reply.append(aiReply);
        response.put("reply", reply.toString());
        response.put("recipes", meals);
        return response;
    }

    public List<String> suggestIngredients(String partial) {
        List<String> knownIngredients = fetchAllIngredients();
        return knownIngredients.stream()
                .filter(ing -> ing.toLowerCase().startsWith(partial.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> fetchRecipes(String ingredient, String area) {
        try {
            String url = "https://www.themealdb.com/api/json/v1/1/filter.php?i=" + ingredient;
            ResponseEntity<Map> res = restTemplate.getForEntity(url, Map.class);
            List<Map<String, String>> meals = (List<Map<String, String>>) res.getBody().get("meals");
            if (meals == null) return Collections.emptyList();

            if (area != null && !area.isEmpty()) {
                return meals.stream().filter(meal -> {
                    String id = meal.get("idMeal");
                    String detailUrl = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + id;
                    ResponseEntity<Map> detailRes = restTemplate.getForEntity(detailUrl, Map.class);
                    List<Map<String, String>> details = (List<Map<String, String>>) detailRes.getBody().get("meals");
                    if (details != null && !details.isEmpty()) {
                        return area.equalsIgnoreCase(details.get(0).get("strArea"));
                    }
                    return false;
                }).collect(Collectors.toList());
            }

            return meals;
        } catch (Exception e) {
            System.err.println("⚠️ Error fetching MealDB recipes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> fetchSpoonacularRecipes(String ingredients) {
        try {
            String url = "https://api.spoonacular.com/recipes/findByIngredients?ingredients=" + ingredients +
                    "&number=5&apiKey=" + spoonacularApiKey;

            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> data = response.getBody();

            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }

            return data.stream().map(item -> {
                Map<String, String> recipe = new HashMap<>();
                recipe.put("strMeal", (String) item.get("title"));
                recipe.put("strMealThumb", (String) item.get("image"));
                recipe.put("idMeal", String.valueOf(item.get("id")));
                recipe.put("source", "spoonacular");
                return recipe;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("⚠️ Error fetching Spoonacular recipes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> fetchAllIngredients() {
        try {
            String url = "https://www.themealdb.com/api/json/v1/1/list.php?i=list";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            List<Map<String, String>> ingredients = (List<Map<String, String>>) response.getBody().get("meals");
            return ingredients.stream()
                    .map(i -> i.get("strIngredient"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("⚠️ Error fetching ingredients: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> applyFilters(List<Map<String, String>> meals, Map<String, Boolean> filters) {
        if (filters == null || filters.isEmpty()) return meals;

        return meals.stream().filter(meal -> {
            String name = meal.getOrDefault("strMeal", "").toLowerCase();
            if (Boolean.TRUE.equals(filters.get("vegetarian")) && !name.contains("vegetarian")) return false;
            if (Boolean.TRUE.equals(filters.get("lowFat")) && name.contains("fat")) return false;
            if (Boolean.TRUE.equals(filters.get("under30")) && name.length() > 120) return false;
            return true;
        }).collect(Collectors.toList());
    }
}
