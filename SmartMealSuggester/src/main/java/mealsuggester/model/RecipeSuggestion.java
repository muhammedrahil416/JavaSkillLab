package mealsuggester.model;

import java.util.List;

/**
 * Holds all scoring and matching results for a single recipe recommendation.
 * This is what gets displayed to the user.
 */
public class RecipeSuggestion {

    private final Recipe recipe;
    private final double totalScore;

    private final List<String> matchedIngredients;
    private final List<String> missingIngredients;
    private final List<String> urgentIngredientsUsed; // matched HIGH-priority ingredients

    // Individual score components (kept separate for easy explanation)
    private final double overlapRatio;
    private final double expiryUrgencyRatio;
    private final double missingPenalty;

    private final String explanation;

    public RecipeSuggestion(
            Recipe recipe,
            double totalScore,
            List<String> matchedIngredients,
            List<String> missingIngredients,
            List<String> urgentIngredientsUsed,
            double overlapRatio,
            double expiryUrgencyRatio,
            double missingPenalty,
            String explanation) {

        this.recipe = recipe;
        this.totalScore = totalScore;
        this.matchedIngredients = matchedIngredients;
        this.missingIngredients = missingIngredients;
        this.urgentIngredientsUsed = urgentIngredientsUsed;
        this.overlapRatio = overlapRatio;
        this.expiryUrgencyRatio = expiryUrgencyRatio;
        this.missingPenalty = missingPenalty;
        this.explanation = explanation;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public List<String> getMatchedIngredients() {
        return matchedIngredients;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    public List<String> getUrgentIngredientsUsed() {
        return urgentIngredientsUsed;
    }

    public double getOverlapRatio() {
        return overlapRatio;
    }

    public double getExpiryUrgencyRatio() {
        return expiryUrgencyRatio;
    }

    public double getMissingPenalty() {
        return missingPenalty;
    }

    public String getExplanation() {
        return explanation;
    }
}

