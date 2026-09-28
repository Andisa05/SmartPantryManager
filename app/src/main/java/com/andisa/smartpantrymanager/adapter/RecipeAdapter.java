package com.andisa.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andisa.smartpantrymanager.R;
import com.andisa.smartpantrymanager.model.Recipe;

import java.util.List;

/**
 * RecyclerView adapter used for both the strict "Suggested Recipes"
 * list and the bonus "Almost There" list on SuggestedRecipesActivity.
 * When a recipe is "almost there", a subtitle naming the single
 * missing ingredient is shown so the list is clearly distinguishable
 * from the strict suggestions, as required by Section 2.3 of the brief.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipeData;
    private final boolean showMissingIngredient;
    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(List<Recipe> recipeData, boolean showMissingIngredient, OnRecipeClickListener listener) {
        this.recipeData = recipeData;
        this.showMissingIngredient = showMissingIngredient;
        this.clickListener = listener;
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView textRecipeName;
        private final TextView textRecipeSubtitle;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textRecipeSubtitle = itemView.findViewById(R.id.textRecipeSubtitle);
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recipe, parent, false);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeData.get(position);
        holder.textRecipeName.setText(recipe.getName());

        if (showMissingIngredient && !recipe.getMissingIngredients().isEmpty()) {
            holder.textRecipeSubtitle.setVisibility(View.VISIBLE);
            holder.textRecipeSubtitle.setText("Missing: " + recipe.getMissingIngredients().get(0));
        } else {
            holder.textRecipeSubtitle.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeData.size();
    }
}
