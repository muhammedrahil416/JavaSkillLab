package mealsuggester;

import mealsuggester.data.RecipeLoader;
import mealsuggester.model.ExpiryPriority;
import mealsuggester.model.Recipe;
import mealsuggester.model.RecipeSuggestion;
import mealsuggester.service.RecipeRankingEngine;
import mealsuggester.util.IngredientNormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Entry point for the Smart Meal & Recipe Suggestion Engine.
 *
 * Handles all user input/output via the command line.
 * Business logic is delegated to the service layer.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        // Load recipes from CSV
        RecipeLoader loader = new RecipeLoader();
        List<Recipe> recipes = loader.loadRecipes();

        if (recipes.isEmpty()) {
            System.out.println("[ERROR] No recipes loaded. Please check recipes.csv.");
            return;
        }
        System.out.println("Loaded " + recipes.size() + " recipes from dataset.\n");

        Scanner scanner = new Scanner(System.in);

        // --- Step 1: Get available ingredients ---
        Set<String> userInventory = collectIngredients(scanner);
        if (userInventory.isEmpty()) {
            System.out.println("No ingredients entered. Exiting.");
            return;
        }

        // --- Step 2: Get expiry priorities ---
        Map<String, ExpiryPriority> expiryPriorities = collectExpiryPriorities(scanner, userInventory);

        // --- Step 3: Get excluded ingredients ---
        Set<String> excludedItems = collectExcludedIngredients(scanner);

        // --- Step 4: Get number of recommendations ---
        int topN = collectTopN(scanner);

        scanner.close();

        // --- Step 5: Run ranking engine ---
        RecipeRankingEngine engine = new RecipeRankingEngine();
        List<RecipeSuggestion> suggestions = engine.rank(
                recipes, userInventory, expiryPriorities, excludedItems, topN);

        // --- Step 6: Display results ---
        printResults(suggestions, userInventory, excludedItems);
    }

    // -------------------------------------------------------------------------
    // Input collection methods
    // -------------------------------------------------------------------------

    /**
     * Prompts the user to enter available ingredients as a comma-separated list.
     */
    private static Set<String> collectIngredients(Scanner scanner) {
        System.out.println("========================================");
        System.out.println("  STEP 1: Enter Your Available Ingredients");
        System.out.println("========================================");
        System.out.println("Enter ingredients separated by commas.");
        System.out.println("Example: tomato, onion, spinach, paneer, garlic");
        System.out.print("\nIngredients: ");

        String input = scanner.nextLine().trim();
        return parseIngredientList(input);
    }

    /**
     * For each ingredient the user has, optionally assigns an expiry priority.
     */
    private static Map<String, ExpiryPriority> collectExpiryPriorities(
            Scanner scanner, Set<String> inventory) {

        System.out.println("\n========================================");
        System.out.println("  STEP 2: Set Expiry Priorities (Optional)");
        System.out.println("========================================");
        System.out.println("Assign expiry priority for ingredients that need to be used soon.");
        System.out.println("Options: HIGH, MEDIUM, LOW  (press Enter to skip)");
        System.out.println("Format: ingredient=PRIORITY (one per line, blank line to finish)");
        System.out.println("Example: spinach=HIGH");

        Map<String, ExpiryPriority> priorities = new HashMap<>();

        System.out.println("\nYour ingredients: " + String.join(", ", inventory));
        System.out.println();

        while (true) {
            System.out.print("Priority (or press Enter to skip/finish): ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                break; // User is done entering priorities
            }

            String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                System.out.println("  [!] Invalid format. Use: ingredient=PRIORITY");
                continue;
            }

            String ingredient = IngredientNormalizer.normalize(parts[0]);
            String priorityText = parts[1].trim().toUpperCase();

            if (!inventory.contains(ingredient)) {
                System.out.println("  [!] '" + ingredient + "' is not in your inventory. Skipping.");
                continue;
            }

            ExpiryPriority priority = parseExpiryPriority(priorityText);
            if (priority == null) {
                System.out.println("  [!] Unknown priority '" + priorityText
                        + "'. Use HIGH, MEDIUM, or LOW.");
                continue;
            }

            priorities.put(ingredient, priority);
            System.out.println("  Set: " + ingredient + " = " + priority);
        }

        return priorities;
    }

    /**
     * Asks the user for ingredients they want excluded from all recipes.
     */
    private static Set<String> collectExcludedIngredients(Scanner scanner) {
        System.out.println("\n========================================");
        System.out.println("  STEP 3: Enter Ingredients to Exclude (Optional)");
        System.out.println("========================================");
        System.out.println("Recipes containing these ingredients will be removed.");
        System.out.println("Example: mushroom, peanut");
        System.out.print("\nExcluded ingredients (or press Enter to skip): ");

        String input = scanner.nextLine().trim();
        return parseIngredientList(input);
    }

    /**
     * Asks how many top recommendations to display.
     */
    private static int collectTopN(Scanner scanner) {
        System.out.println("\n========================================");
        System.out.println("  STEP 4: How Many Recommendations?");
        System.out.println("========================================");
        System.out.print("Number of recommendations to display (default 5): ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return 5;
        }

        try {
            int n = Integer.parseInt(input);
            if (n <= 0) {
                System.out.println("  [!] Must be > 0. Using default: 5");
                return 5;
            }
            return n;
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid number. Using default: 5");
            return 5;
        }
    }

    // -------------------------------------------------------------------------
    // Output methods
    // -------------------------------------------------------------------------

    private static void printBanner() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("     SMART MEAL SUGGESTION ENGINE");
        System.out.println("========================================");
        System.out.println();
    }

    /**
     * Prints all ranked recipe suggestions in a clean, readable format.
     */
    private static void printResults(
            List<RecipeSuggestion> suggestions,
            Set<String> userInventory,
            Set<String> excludedItems) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("         TOP RECOMMENDATIONS");
        System.out.println("========================================");

        if (suggestions.isEmpty()) {
            System.out.println("\nNo recipes match your current inventory and criteria.");
            System.out.println("Try adding more ingredients or relaxing exclusion filters.");
            return;
        }

        for (int i = 0; i < suggestions.size(); i++) {
            RecipeSuggestion s = suggestions.get(i);
            Recipe r = s.getRecipe();

            System.out.println();
            System.out.println("#" + (i + 1) + "  " + r.getName());
            System.out.printf("Score: %.1f%n", s.getTotalScore());
            System.out.printf("Match: %d/%d (%d%%)%n",
                    s.getMatchedIngredients().size(),
                    r.getIngredients().size(),
                    (int) Math.round(s.getOverlapRatio() * 100));
            System.out.println("Prep Time: " + r.getPrepTimeMinutes() + " min");

            System.out.println("Matched : " + formatList(s.getMatchedIngredients()));
            System.out.println("Missing : " + formatList(s.getMissingIngredients()));

            if (!s.getUrgentIngredientsUsed().isEmpty()) {
                System.out.println("Urgent  : " + formatList(s.getUrgentIngredientsUsed()));
            }

            System.out.println();
            System.out.println("Why suggested:");
            System.out.println(s.getExplanation());
            System.out.println();
            System.out.println("----------------------------------------");
        }

        printSummary(suggestions, userInventory, excludedItems);
    }

    /**
     * Prints a brief summary at the end of the output.
     */
    private static void printSummary(
            List<RecipeSuggestion> suggestions,
            Set<String> userInventory,
            Set<String> excludedItems) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("                 SUMMARY");
        System.out.println("========================================");
        System.out.println("Recipes shown   : " + suggestions.size());
        System.out.println("Your inventory  : " + String.join(", ", userInventory));

        if (!excludedItems.isEmpty()) {
            System.out.println("Excluded        : " + String.join(", ", excludedItems));
        }

        // Collect all unique missing ingredients across all suggestions
        Set<String> allMissing = new HashSet<>();
        for (RecipeSuggestion s : suggestions) {
            allMissing.addAll(s.getMissingIngredients());
        }
        if (!allMissing.isEmpty()) {
            System.out.println("Shopping list   : " + String.join(", ", allMissing));
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("  Thank you for using Smart Meal Engine!");
        System.out.println("========================================");
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    /**
     * Parses a comma-separated ingredient string into a normalized Set.
     */
    private static Set<String> parseIngredientList(String input) {
        Set<String> result = new HashSet<>();
        if (input == null || input.isEmpty()) {
            return result;
        }

        String[] parts = input.split(",");
        for (String part : parts) {
            String normalized = IngredientNormalizer.normalize(part);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    /**
     * Parses a priority string like "HIGH" into an ExpiryPriority enum value.
     * Returns null if unrecognized.
     */
    private static ExpiryPriority parseExpiryPriority(String text) {
        switch (text) {
            case "HIGH":   return ExpiryPriority.HIGH;
            case "MEDIUM": return ExpiryPriority.MEDIUM;
            case "LOW":    return ExpiryPriority.LOW;
            default:       return null;
        }
    }

    /**
     * Formats a list of strings into a comma-separated display string.
     * Returns "(none)" if the list is empty.
     */
    private static String formatList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "(none)";
        }
        return String.join(", ", items);
    }
}

