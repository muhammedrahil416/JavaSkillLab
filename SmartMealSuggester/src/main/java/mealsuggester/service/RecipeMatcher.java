package mealsuggester.service;

import mealsuggester.model.ExpiryPriority;
import mealsuggester.model.Recipe;
import mealsuggester.model.RecipeSuggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Matches a single recipe against the user's available inventory.
 *
 * Steps:
 * 1. Reject if any excluded ingredient is present.
 * 2. Find matched and missing ingredients.
 * 3. Check overlap ratio >= 0.25.
 * 4. Calculate score components.
 * 5. Build and return a RecipeSuggestion.
 *
 * Returns null if the recipe is rejected.
 */
public class RecipeMatcher {

    private static final double MIN_OVERLAP_RATIO = 0.25;

    /**
     * Attempts to match a recipe against the user's inventory.
     *
     * @param recipe           the recipe to evaluate
     * @param userInventory    normalized set of ingredients the user has
     * @param expiryPriorities map of ingredient -> ExpiryPriority
     * @param excludedItems    normalized set of ingredients to exclude
     * @return a RecipeSuggestion if the recipe qualifies, or null if rejected
     */
    public RecipeSuggestion match(
            Recipe recipe,
            Set<String> userInventory,
            Map<String, ExpiryPriority> expiryPriorities,
            Set<String> excludedItems) {

        // Step 1: Exclusion filter — reject immediately if any excluded ingredient is present
        String excludedFound = findExcludedIngredient(recipe, excludedItems);
        if (excludedFound != null) {
            return null; // Recipe is disqualified
        }

        // Step 2: Split recipe ingredients into matched and missing
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String ingredient : recipe.getIngredients()) {
            if (userInventory.contains(ingredient)) {
                matched.add(ingredient);
            } else {
                missing.add(ingredient);
            }
        }

        int totalRecipeIngredients = recipe.getIngredients().size();

        // Step 3: Overlap ratio check
        double overlapRatio = calculateOverlapRatio(matched.size(), totalRecipeIngredients);
        if (overlapRatio < MIN_OVERLAP_RATIO) {
            return null; // Not enough ingredient overlap
        }

        // Step 4: Collect HIGH-priority matched ingredients
        List<String> urgentIngredients = findUrgentIngredients(matched, expiryPriorities);

        // Step 5: Calculate individual score components
        double expiryUrgencyRatio = calculateExpiryUrgencyRatio(
                matched, expiryPriorities, totalRecipeIngredients);
        double missingPenalty = calculateMissingPenalty(missing.size());
        double totalScore = calculateTotalScore(overlapRatio, expiryUrgencyRatio, missingPenalty);

        // Step 6: Generate human-readable explanation
        String explanation = ExplanationGenerator.generate(
                recipe, matched, missing, urgentIngredients, overlapRatio, totalRecipeIngredients);

        return new RecipeSuggestion(
                recipe,
                totalScore,
                matched,
                missing,
                urgentIngredients,
                overlapRatio,
                expiryUrgencyRatio,
                missingPenalty,
                explanation);
    }

    /**
     * Returns the first excluded ingredient found in the recipe, or null if none.
     */
    private String findExcludedIngredient(Recipe recipe, Set<String> excludedItems) {
        for (String ingredient : recipe.getIngredients()) {
            if (excludedItems.contains(ingredient)) {
                return ingredient;
            }
        }
        return null;
    }

    /**
     * overlapRatio = matchedCount / totalRecipeIngredients
     */
    private double calculateOverlapRatio(int matchedCount, int totalIngredients) {
        if (totalIngredients == 0) return 0.0;
        return (double) matchedCount / totalIngredients;
    }

    /**
     * expiryUrgencyRatio = sum(priority values of matched) / (3 * totalRecipeIngredients)
     *
     * Ingredients without an assigned priority are treated as LOW (value = 1).
     */
    private double calculateExpiryUrgencyRatio(
            List<String> matched,
            Map<String, ExpiryPriority> expiryPriorities,
            int totalIngredients) {

        if (totalIngredients == 0) return 0.0;

        int prioritySum = 0;
        for (String ingredient : matched) {
            ExpiryPriority priority = expiryPriorities.getOrDefault(ingredient, ExpiryPriority.LOW);
            prioritySum += priority.getValue();
        }

        int maxPossibleSum = 3 * totalIngredients; // all ingredients at HIGH
        return (double) prioritySum / maxPossibleSum;
    }

    /**
     * missingPenalty = min(1.0, missingCount / 5.0)
     */
    private double calculateMissingPenalty(int missingCount) {
        return Math.min(1.0, missingCount / 5.0);
    }

    /**
     * TotalScore = (50 × overlapRatio) + (40 × expiryUrgencyRatio) - (10 × missingPenalty)
     */
    private double calculateTotalScore(
            double overlapRatio,
            double expiryUrgencyRatio,
            double missingPenalty) {

        return (50 * overlapRatio)
             + (40 * expiryUrgencyRatio)
             - (10 * missingPenalty);
    }

    /**
     * Returns matched ingredients that have HIGH expiry priority.
     */
    private List<String> findUrgentIngredients(
            List<String> matched,
            Map<String, ExpiryPriority> expiryPriorities) {

        List<String> urgent = new ArrayList<>();
        for (String ingredient : matched) {
            if (expiryPriorities.get(ingredient) == ExpiryPriority.HIGH) {
                urgent.add(ingredient);
            }
        }
        return urgent;
    }
}

