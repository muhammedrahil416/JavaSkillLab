package mealsuggester.model;

import java.util.List;

/**
 * Represents a single recipe loaded from the CSV dataset.
 */
public class Recipe {

    private final int id;
    private final String name;
    private final List<String> ingredients; // normalized ingredient names
    private final int prepTimeMinutes;

    public Recipe(int id, String name, List<String> ingredients, int prepTimeMinutes) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    @Override
    public String toString() {
        return "Recipe{id=" + id + ", name='" + name + "', prepTime=" + prepTimeMinutes + "min}";
    }
}

