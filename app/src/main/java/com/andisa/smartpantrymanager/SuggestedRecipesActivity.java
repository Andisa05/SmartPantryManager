package com.andisa.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andisa.smartpantrymanager.adapter.RecipeAdapter;
import com.andisa.smartpantrymanager.data.PantryDataSource;
import com.andisa.smartpantrymanager.model.Recipe;

/**
 * Runs the strict-matching rule (Section 2.3) against the current
 * pantry and shows only the recipes the user can make right now.
 * A separate "Almost There" list (missing exactly one ingredient)
 * is shown underneath as a clearly-labelled bonus feature - it is
 * never merged into the strict suggestions list.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerSuggested;
    private RecyclerView recyclerAlmostThere;
    private TextView textEmptySuggestions;
    private TextView textAlmostThereHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerSuggested = findViewById(R.id.recyclerSuggested);
        recyclerAlmostThere = findViewById(R.id.recyclerAlmostThere);
        textEmptySuggestions = findViewById(R.id.textEmptySuggestions);
        textAlmostThereHeader = findViewById(R.id.textAlmostThereHeader);

        recyclerSuggested.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmostThere.setLayoutManager(new LinearLayoutManager(this));

        initPantryButton();
        initRecipesButton();
        initSettingsButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            PantryDataSource.MatchResult result = ds.getSuggestedRecipes();
            ds.close();

            RecipeAdapter suggestedAdapter = new RecipeAdapter(result.suggested, false, this::openRecipeDetail);
            recyclerSuggested.setAdapter(suggestedAdapter);

            boolean hasSuggestions = !result.suggested.isEmpty();
            textEmptySuggestions.setVisibility(hasSuggestions ? View.GONE : View.VISIBLE);
            recyclerSuggested.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);

            boolean hasAlmostThere = !result.almostThere.isEmpty();
            textAlmostThereHeader.setVisibility(hasAlmostThere ? View.VISIBLE : View.GONE);
            recyclerAlmostThere.setVisibility(hasAlmostThere ? View.VISIBLE : View.GONE);
            if (hasAlmostThere) {
                RecipeAdapter almostThereAdapter = new RecipeAdapter(result.almostThere, true, this::openRecipeDetail);
                recyclerAlmostThere.setAdapter(almostThereAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error matching recipes", Toast.LENGTH_LONG).show();
        }
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        startActivity(intent);
    }

    // -----------------------------------------------------------
    // Bottom navigation bar
    // -----------------------------------------------------------

    private void initPantryButton() {
        ImageButton ib = findViewById(R.id.imageButtonPantry);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, PantryListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initRecipesButton() {
        ImageButton ib = findViewById(R.id.imageButtonRecipes);
        ib.setEnabled(false);
    }

    private void initSettingsButton() {
        ImageButton ib = findViewById(R.id.imageButtonSettings);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
