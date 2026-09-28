package com.andisa.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe: a name, its required ingredients, and a simple method
 * (preparation steps). Used both for the seeded recipe collection
 * and for recipes retrieved from the database.
 */
public class Recipe {

    private int id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients;

    // Filled in only when a recipe has been run through the
    // strict-matching algorithm - see IngredientMatcher /
    // PantryDataSource.getSuggestedRecipes(). Not persisted.
    private List<String> missingIngredients;

    public Recipe() {
        id = -1;
        ingredients = new ArrayList<>();
        missingIngredients = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    public void setMissingIngredients(List<String> missingIngredients) {
        this.missingIngredients = missingIngredients;
    }
}
