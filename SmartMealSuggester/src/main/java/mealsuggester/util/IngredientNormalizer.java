package mealsuggester.util;

/**
 * Normalizes ingredient names so that "Tomatoes", " TOMATO ", "tomatoes"
 * all become "tomato". This ensures consistent comparisons between
 * user input and dataset ingredients.
 */
public class IngredientNormalizer {

    /**
     * Normalize a single ingredient name:
     * 1. Trim leading/trailing whitespace
     * 2. Convert to lowercase
     * 3. Strip simple plural forms (trailing 's' and 'es')
     */
    public static String normalize(String ingredient) {
        if (ingredient == null) {
            return "";
        }

        String normalized = ingredient.trim().toLowerCase();

        // Remove trailing "es" before "s" check (e.g., "tomatoes" -> "tomato")
        if (normalized.endsWith("es") && normalized.length() > 3) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        // Remove trailing "s" for simple plurals (e.g., "onions" -> "onion")
        // But avoid stripping words that naturally end in 's' (e.g., "asparagus", "hummus")
        else if (normalized.endsWith("s") && normalized.length() > 3
                && !normalized.endsWith("ss")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}

