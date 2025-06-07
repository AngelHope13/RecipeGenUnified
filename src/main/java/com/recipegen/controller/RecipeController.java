package com.recipegen.controller;

import com.recipegen.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/recipes")
    public String getRecipes(@RequestBody Map<String, Object> payload) {
        Object ingredientsObj = payload.get("ingredients");
        String nationality = (String) payload.get("nationality");

        if (!(ingredientsObj instanceof List<?> ingredients)) {
            return "Invalid ingredients input.";
        }

        if (nationality == null) {
            nationality = "";
        }

        return recipeService.getRecipesByIngredientAndNationality(
                ingredients.stream().map(Object::toString).toList(),
                nationality
        );
    }
}
