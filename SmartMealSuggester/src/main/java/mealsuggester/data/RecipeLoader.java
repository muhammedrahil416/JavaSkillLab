package mealsuggester.data;

import mealsuggester.model.Recipe;
import mealsuggester.util.IngredientNormalizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads recipes from the CSV file bundled in resources.
 *
 * Expected CSV format (first line is header):
 *   id,name,ingredients,prepTime
 *
 * The "ingredients" column is a pipe-separated list inside the CSV cell.
 * Example: "1,Pasta Primavera,pasta|tomato|garlic|onion|olive oil,20"
 */
public class RecipeLoader {

    private static final String CSV_RESOURCE_PATH = "/recipes.csv";
    private static final String COLUMN_SEPARATOR = ",";
    private static final String INGREDIENT_SEPARATOR = "\\|"; // pipe char in regex

    /**
     * Loads all recipes from the bundled CSV file.
     *
     * @return list of Recipe objects; empty list if the file cannot be read
     */
    public List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        InputStream stream = getClass().getResourceAsStream(CSV_RESOURCE_PATH);
        if (stream == null) {
            System.err.println("[ERROR] recipes.csv not found in resources. "
                    + "Make sure it is on the classpath.");
            return recipes;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip header row and blank lines
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (line.isEmpty()) {
                    continue;
                }

                Recipe recipe = parseLine(line);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to read recipes.csv: " + e.getMessage());
        }

        return recipes;
    }

    /**
     * Parses one CSV line into a Recipe object.
     * Returns null if the line is malformed.
     */
    private Recipe parseLine(String line) {
        // We split on comma but only the first 3 commas (4 fields total)
        // because the name might not contain commas in our dataset.
        String[] parts = line.split(COLUMN_SEPARATOR, 4);

        if (parts.length < 4) {
            System.err.println("[WARN] Skipping malformed CSV line: " + line);
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String ingredientsRaw = parts[2].trim();
            int prepTime = Integer.parseInt(parts[3].trim());

            List<String> ingredients = parseIngredients(ingredientsRaw);

            return new Recipe(id, name, ingredients, prepTime);

        } catch (NumberFormatException e) {
            System.err.println("[WARN] Skipping CSV line with invalid number: " + line);
            return null;
        }
    }

    /**
     * Splits and normalizes the pipe-separated ingredient list.
     */
    private List<String> parseIngredients(String ingredientsRaw) {
        String[] raw = ingredientsRaw.split(INGREDIENT_SEPARATOR);
        List<String> normalized = new ArrayList<>();

        for (String ingredient : raw) {
            String n = IngredientNormalizer.normalize(ingredient);
            if (!n.isEmpty()) {
                normalized.add(n);
            }
        }
        return normalized;
    }
}

