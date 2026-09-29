package com.andisa.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.andisa.smartpantrymanager.model.PantryItem;
import com.andisa.smartpantrymanager.model.Recipe;
import com.andisa.smartpantrymanager.model.RecipeIngredient;
import com.andisa.smartpantrymanager.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Source class - same role as ContactDataSource in the module:
 * opens/closes the database and provides every query and CRUD
 * method the Activities need. Raw SQL + Cursor is used throughout,
 * matching the approach taught for SQLiteOpenHelper-based apps.
 */
public class PantryDataSource {

    private SQLiteDatabase database;
    private final PantryDBHelper dbHelper;

    public PantryDataSource(Context context) {
        dbHelper = new PantryDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ---------------------------------------------------------------
    // Pantry item CRUD (Create, Read, Update, Delete)
    // ---------------------------------------------------------------

    public boolean insertPantryItem(PantryItem item) {
        boolean didSucceed = false;
        try {
            ContentValues values = new ContentValues();
            values.put("name", item.getName());
            values.put("quantity", item.getQuantity());
            values.put("unit", item.getUnit());
            values.put("expiry_date", item.getExpiryDate());
            didSucceed = database.insert(PantryDBHelper.TABLE_PANTRY, null, values) > 0;
        } catch (Exception e) {
            // didSucceed already false
        }
        return didSucceed;
    }

    public boolean updatePantryItem(PantryItem item) {
        boolean didSucceed = false;
        try {
            ContentValues values = new ContentValues();
            values.put("name", item.getName());
            values.put("quantity", item.getQuantity());
            values.put("unit", item.getUnit());
            values.put("expiry_date", item.getExpiryDate());
            didSucceed = database.update(PantryDBHelper.TABLE_PANTRY, values,
                    "_id=" + item.getId(), null) > 0;
        } catch (Exception e) {
            // didSucceed already false
        }
        return didSucceed;
    }

    public boolean deletePantryItem(int id) {
        boolean didDelete = false;
        try {
            didDelete = database.delete(PantryDBHelper.TABLE_PANTRY, "_id=" + id, null) > 0;
        } catch (Exception e) {
            // didDelete already false
        }
        return didDelete;
    }

    public ArrayList<PantryItem> getAllPantryItems() {
        ArrayList<PantryItem> items = new ArrayList<>();
        try {
            String query = "SELECT * FROM " + PantryDBHelper.TABLE_PANTRY + " ORDER BY name";
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                items.add(pantryItemFromCursor(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            items = new ArrayList<>();
        }
        return items;
    }

    public PantryItem getPantryItem(int id) {
        PantryItem item = new PantryItem();
        try {
            String query = "SELECT * FROM " + PantryDBHelper.TABLE_PANTRY + " WHERE _id=" + id;
            Cursor cursor = database.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                item = pantryItemFromCursor(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            // return the empty (new) item
        }
        return item;
    }

    private PantryItem pantryItemFromCursor(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getInt(0));
        item.setName(cursor.getString(1));
        item.setQuantity(cursor.getDouble(2));
        item.setUnit(cursor.getString(3));
        item.setExpiryDate(cursor.isNull(4) ? -1L : cursor.getLong(4));
        return item;
    }

    // ---------------------------------------------------------------
    // Recipe reads (the recipe collection is seeded once in
    // PantryDBHelper.onCreate and is otherwise read-only)
    // ---------------------------------------------------------------

    public ArrayList<Recipe> getAllRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            String query = "SELECT * FROM " + PantryDBHelper.TABLE_RECIPE + " ORDER BY name";
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Recipe recipe = new Recipe();
                recipe.setId(cursor.getInt(0));
                recipe.setName(cursor.getString(1));
                recipe.setSteps(cursor.getString(2));
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                recipes.add(recipe);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            recipes = new ArrayList<>();
        }
        return recipes;
    }

    public Recipe getRecipe(int recipeId) {
        Recipe recipe = new Recipe();
        try {
            String query = "SELECT * FROM " + PantryDBHelper.TABLE_RECIPE + " WHERE _id=" + recipeId;
            Cursor cursor = database.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                recipe.setId(cursor.getInt(0));
                recipe.setName(cursor.getString(1));
                recipe.setSteps(cursor.getString(2));
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
            }
            cursor.close();
        } catch (Exception e) {
            // return the empty recipe
        }
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(int recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String query = "SELECT ingredient_name, quantity, unit FROM " + PantryDBHelper.TABLE_RECIPE_INGREDIENT
                + " WHERE recipe_id=" + recipeId;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ingredients.add(new RecipeIngredient(
                    cursor.getString(0), cursor.getDouble(1), cursor.getString(2)));
            cursor.moveToNext();
        }
        cursor.close();
        return ingredients;
    }

    // ---------------------------------------------------------------
    // The strict-matching rule (Section 2.3 of the brief) - this is
    // the single most important piece of business logic in the app.
    // ---------------------------------------------------------------

    /** Result of running the pantry against every recipe. */
    public static class MatchResult {
        public final List<Recipe> suggested = new ArrayList<>();
        public final List<Recipe> almostThere = new ArrayList<>();
    }

    /**
     * Runs every recipe against the current pantry contents. A
     * recipe qualifies for {@link MatchResult#suggested} only if
     * every one of its ingredients is present in the pantry in at
     * least the required quantity (Section 2.3). A recipe missing
     * exactly one ingredient is placed in {@link MatchResult#almostThere}
     * as a separate, clearly-labelled bonus list - it is never
     * mixed into the strict suggestions.
     */
    public MatchResult getSuggestedRecipes() {
        MatchResult result = new MatchResult();

        List<PantryItem> pantryItems = getAllPantryItems();
        // Group pantry quantities by normalised ingredient name so that
        // e.g. two separate "tomato" entries are combined for matching.
        Map<String, List<PantryItem>> pantryByName = new HashMap<>();
        for (PantryItem item : pantryItems) {
            String key = IngredientMatcher.normalizeName(item.getName());
            if (!pantryByName.containsKey(key)) {
                pantryByName.put(key, new ArrayList<PantryItem>());
            }
            pantryByName.get(key).add(item);
        }

        List<Recipe> allRecipes = getAllRecipes();
        for (Recipe recipe : allRecipes) {
            List<String> missing = new ArrayList<>();

            for (RecipeIngredient required : recipe.getIngredients()) {
                if (!isIngredientSatisfied(required, pantryByName)) {
                    missing.add(required.getName());
                }
            }
            // no missing ingredients suggested only one missing
            recipe.setMissingIngredients(missing);
            if (missing.isEmpty()) {
                result.suggested.add(recipe);
            } else if (missing.size() == 1) {
                result.almostThere.add(recipe);
            }
        }
        return result;
    }

    private boolean isIngredientSatisfied(RecipeIngredient required, Map<String, List<PantryItem>> pantryByName) {
        String key = IngredientMatcher.normalizeName(required.getName());
        List<PantryItem> matches = pantryByName.get(key);
        if (matches == null || matches.isEmpty()) {
            return false;
        }

        String requiredUnit = IngredientMatcher.normalizeUnit(required.getUnit());
        IngredientMatcher.UnitCategory requiredCategory = IngredientMatcher.categoryOf(requiredUnit);
        double requiredBaseQty = IngredientMatcher.toBaseQuantity(required.getQuantity(), requiredUnit);

        double availableBaseQty = 0;
        for (PantryItem pantryItem : matches) {
            String pantryUnit = IngredientMatcher.normalizeUnit(pantryItem.getUnit());
            IngredientMatcher.UnitCategory pantryCategory = IngredientMatcher.categoryOf(pantryUnit);
            if (pantryCategory != requiredCategory || pantryCategory == IngredientMatcher.UnitCategory.UNKNOWN) {
                // Units are for different kinds of measurement (e.g. mass vs
                // volume) and cannot be safely compared - skip this entry.
                continue;
            }
            availableBaseQty += IngredientMatcher.toBaseQuantity(pantryItem.getQuantity(), pantryUnit);
        }

        return availableBaseQty >= requiredBaseQty;
    }
}
