package com.andisa.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.andisa.smartpantrymanager.data.PantryDataSource;
import com.andisa.smartpantrymanager.model.Recipe;
import com.andisa.smartpantrymanager.model.RecipeIngredient;

/**
 * Shows the full ingredient list and preparation method for a
 * single recipe, selected from either the Suggested Recipes or
 * Almost There list. The recipe's database ID is passed via an
 * Intent extra, the same "pass an ID, look the record up here"
 * pattern used throughout the module.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    private TextView textName;
    private TextView textIngredients;
    private TextView textSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textName = findViewById(R.id.textRecipeDetailName);
        textIngredients = findViewById(R.id.textRecipeDetailIngredients);
        textSteps = findViewById(R.id.textRecipeDetailSteps);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("recipeId")) {
            loadRecipe(extras.getInt("recipeId"));
        }

        initPantryButton();
        initRecipesButton();
        initSettingsButton();
    }

    private void loadRecipe(int recipeId) {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            Recipe recipe = ds.getRecipe(recipeId);
            ds.close();

            textName.setText(recipe.getName());

            StringBuilder ingredientsText = new StringBuilder();
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ingredientsText.append("- ")
                        .append(trimTrailingZero(ingredient.getQuantity()))
                        .append(" ")
                        .append(ingredient.getUnit())
                        .append(" ")
                        .append(ingredient.getName())
                        .append("\n");
            }
            textIngredients.setText(ingredientsText.toString().trim());
            textSteps.setText(recipe.getSteps());
        } catch (Exception e) {
            Toast.makeText(this, "Could not load recipe", Toast.LENGTH_LONG).show();
        }
    }

    private String trimTrailingZero(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    // -----------------------------------------------------------
    // Bottom navigation bar
    // -----------------------------------------------------------

    private void initPantryButton() {
        ImageButton ib = findViewById(R.id.imageButtonPantry);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, PantryListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initRecipesButton() {
        ImageButton ib = findViewById(R.id.imageButtonRecipes);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, SuggestedRecipesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initSettingsButton() {
        ImageButton ib = findViewById(R.id.imageButtonSettings);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
