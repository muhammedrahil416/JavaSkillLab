package mealsuggester.service;

import mealsuggester.model.ExpiryPriority;
import mealsuggester.model.Recipe;
import mealsuggester.model.RecipeSuggestion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orchestrates the full recipe ranking pipeline:
 * 1. Run RecipeMatcher on every recipe.
 * 2. Collect qualifying suggestions.
 * 3. Sort by the four-tier comparator.
 * 4. Return the top-N results.
 */
public class RecipeRankingEngine {

    private final RecipeMatcher matcher;

    public RecipeRankingEngine() {
        this.matcher = new RecipeMatcher();
    }

    /**
     * Ranks all recipes and returns the top {@code topN} suggestions.
     *
     * @param recipes          full recipe dataset
     * @param userInventory    normalized set of ingredients the user has
     * @param expiryPriorities ingredient -> priority map
     * @param excludedItems    normalized set of excluded ingredients
     * @param topN             maximum number of suggestions to return
     * @return sorted list of RecipeSuggestion (best first)
     */
    public List<RecipeSuggestion> rank(
            List<Recipe> recipes,
            Set<String> userInventory,
            Map<String, ExpiryPriority> expiryPriorities,
            Set<String> excludedItems,
            int topN) {

        List<RecipeSuggestion> qualifyingSuggestions = new ArrayList<>();

        // Evaluate every recipe
        for (Recipe recipe : recipes) {
            RecipeSuggestion suggestion = matcher.match(
                    recipe, userInventory, expiryPriorities, excludedItems);

            if (suggestion != null) {
                qualifyingSuggestions.add(suggestion);
            }
        }

        // Sort by the four-tier comparator
        qualifyingSuggestions.sort(buildComparator());

        // Return at most topN results
        int limit = Math.min(topN, qualifyingSuggestions.size());
        return qualifyingSuggestions.subList(0, limit);
    }

    /**
     * Builds the comparator that defines ranking order:
     * 1. Total score — descending (higher is better)
     * 2. HIGH-priority ingredients used — descending (more urgent use is better)
     * 3. Missing ingredient count — ascending (fewer missing is better)
     * 4. Preparation time — ascending (quicker recipes preferred)
     */
    private Comparator<RecipeSuggestion> buildComparator() {
        return Comparator
                // Tier 1: higher score first
                .comparingDouble(RecipeSuggestion::getTotalScore).reversed()
                // Tier 2: more HIGH-priority ingredients used first
                .thenComparingInt(s -> -s.getUrgentIngredientsUsed().size())
                // Tier 3: fewer missing ingredients first
                .thenComparingInt(s -> s.getMissingIngredients().size())
                // Tier 4: shorter prep time first
                .thenComparingInt(s -> s.getRecipe().getPrepTimeMinutes());
    }
}

