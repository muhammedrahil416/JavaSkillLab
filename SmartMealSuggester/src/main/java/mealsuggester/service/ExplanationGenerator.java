package mealsuggester.service;

import mealsuggester.model.Recipe;

import java.util.List;

/**
 * Generates human-readable "Why suggested" explanations for each recipe.
 * All text is built from calculated values — no AI or template engine involved.
 */
public class ExplanationGenerator {

    /**
     * Builds the explanation string for a qualifying recipe suggestion.
     *
     * @param recipe              the recipe being suggested
     * @param matched             list of matched ingredient names
     * @param missing             list of missing ingredient names
     * @param urgentIngredients   matched HIGH-priority ingredients
     * @param overlapRatio        fraction of recipe ingredients the user has
     * @param totalIngredients    total number of ingredients in the recipe
     * @return formatted explanation string
     */
    public static String generate(
            Recipe recipe,
            List<String> matched,
            List<String> missing,
            List<String> urgentIngredients,
            double overlapRatio,
            int totalIngredients) {

        StringBuilder sb = new StringBuilder();

        // Line 1: Overlap description
        int overlapPercent = (int) Math.round(overlapRatio * 100);
        sb.append(overlapPercent).append("% ingredient match (")
          .append(matched.size()).append(" of ").append(totalIngredients)
          .append(" ingredients available).");

        // Line 2: Urgent ingredients (only if any exist)
        if (!urgentIngredients.isEmpty()) {
            sb.append("\nUses ").append(urgentIngredients.size())
              .append(" HIGH-priority ingredient");
            if (urgentIngredients.size() > 1) sb.append("s");
            sb.append(": ").append(String.join(", ", urgentIngredients)).append(".");
        }

        // Line 3: Missing ingredients summary
        if (missing.isEmpty()) {
            sb.append("\nAll ingredients are available — no shopping needed!");
        } else if (missing.size() == 1) {
            sb.append("\nMissing only: ").append(missing.get(0)).append(".");
        } else {
            sb.append("\nMissing ").append(missing.size())
              .append(" ingredients: ").append(String.join(", ", missing)).append(".");
        }

        return sb.toString();
    }
}

