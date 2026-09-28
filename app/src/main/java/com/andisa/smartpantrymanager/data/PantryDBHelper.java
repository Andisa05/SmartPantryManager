package com.andisa.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database Helper class - follows the same pattern taught in the
 * module's persistent-data chapter (e.g. ContactDBHelper): a
 * subclass of SQLiteOpenHelper whose only job is to create,
 * upgrade, and (for this app) seed the tables. All day-to-day
 * querying lives in PantryDataSource.
 */
public class PantryDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "PantryDBHelper";

    private static final String DATABASE_NAME = "smartpantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_item";
    public static final String TABLE_RECIPE = "recipe";
    public static final String TABLE_RECIPE_INGREDIENT = "recipe_ingredient";

    private static final String CREATE_TABLE_PANTRY =
            "create table " + TABLE_PANTRY + " (" +
                    "_id integer primary key autoincrement, " +
                    "name text not null, " +
                    "quantity real not null, " +
                    "unit text not null, " +
                    "expiry_date integer);";

    private static final String CREATE_TABLE_RECIPE =
            "create table " + TABLE_RECIPE + " (" +
                    "_id integer primary key autoincrement, " +
                    "name text not null, " +
                    "steps text not null);";

    private static final String CREATE_TABLE_RECIPE_INGREDIENT =
            "create table " + TABLE_RECIPE_INGREDIENT + " (" +
                    "_id integer primary key autoincrement, " +
                    "recipe_id integer not null, " +
                    "ingredient_name text not null, " +
                    "quantity real not null, " +
                    "unit text not null, " +
                    "foreign key(recipe_id) references " + TABLE_RECIPE + "(_id));";

    public PantryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENT);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    /**
     * Pre-loads the recipe collection required by Section 2.2 of
     * the brief (at least 15-20 recipes) on first run. Each entry
     * is {name, steps, {ingredientName, quantity, unit}, ...}.
     */
    private void seedRecipes(SQLiteDatabase db) {
        Object[][] recipes = {
                {"Tomato & Egg Stir-Fry",
                        "1. Beat the eggs with a pinch of salt.\n2. Fry the eggs lightly and set aside.\n" +
                                "3. Fry the chopped onion and tomato in oil until soft.\n4. Return the eggs to the pan, toss together and serve.",
                        new Object[][]{{"tomato", 2, "pcs"}, {"egg", 3, "pcs"}, {"onion", 1, "pcs"}, {"salt", 1, "tsp"}, {"oil", 1, "tbsp"}}},

                {"Vegetable Fried Rice",
                        "1. Cook the rice and let it cool.\n2. Scramble the eggs in a hot pan and set aside.\n" +
                                "3. Stir-fry the chopped carrot and onion, then add the rice.\n4. Mix in the egg and soy sauce and stir-fry until hot.",
                        new Object[][]{{"rice", 2, "cup"}, {"egg", 2, "pcs"}, {"carrot", 1, "pcs"}, {"onion", 1, "pcs"}, {"soy sauce", 2, "tbsp"}}},

                {"Pasta Aglio e Olio",
                        "1. Boil the pasta until al dente.\n2. Gently fry the sliced garlic in olive oil until golden.\n" +
                                "3. Add chili flakes.\n4. Toss the drained pasta through the garlic oil and serve.",
                        new Object[][]{{"pasta", 200, "g"}, {"garlic", 4, "pcs"}, {"olive oil", 3, "tbsp"}, {"chili flakes", 1, "tsp"}}},

                {"Cheese Omelette",
                        "1. Beat the eggs with milk and salt.\n2. Pour into a hot, lightly oiled pan.\n" +
                                "3. Sprinkle the cheese over one half once the base sets.\n4. Fold in half and serve.",
                        new Object[][]{{"egg", 3, "pcs"}, {"cheese", 50, "g"}, {"milk", 30, "ml"}, {"salt", 1, "tsp"}}},

                {"Potato & Onion Hash",
                        "1. Dice the potato and onion.\n2. Fry the potato in oil until starting to brown.\n" +
                                "3. Add the onion and salt, and cook until both are golden and tender.",
                        new Object[][]{{"potato", 3, "pcs"}, {"onion", 1, "pcs"}, {"oil", 2, "tbsp"}, {"salt", 1, "tsp"}}},

                {"Chicken & Rice Soup",
                        "1. Bring the water to a boil and add the chicken.\n2. Simmer for 15 minutes.\n" +
                                "3. Add the rice, carrot and onion.\n4. Simmer until the rice is soft and serve hot.",
                        new Object[][]{{"chicken", 200, "g"}, {"rice", 1, "cup"}, {"carrot", 1, "pcs"}, {"onion", 1, "pcs"}, {"water", 500, "ml"}}},

                {"Banana Pancakes",
                        "1. Mash the banana and whisk in the egg and milk.\n2. Fold in the flour to make a batter.\n" +
                                "3. Cook spoonfuls of batter on a hot, lightly oiled pan until golden on both sides.",
                        new Object[][]{{"banana", 2, "pcs"}, {"flour", 200, "g"}, {"egg", 1, "pcs"}, {"milk", 100, "ml"}}},

                {"Peanut Butter Toast",
                        "1. Toast the bread.\n2. Spread the peanut butter over each slice while warm.",
                        new Object[][]{{"bread", 2, "pcs"}, {"peanut butter", 30, "g"}}},

                {"Grilled Cheese Sandwich",
                        "1. Butter one side of each slice of bread.\n2. Place cheese between the slices, buttered sides out.\n" +
                                "3. Grill in a pan on both sides until the bread is golden and the cheese has melted.",
                        new Object[][]{{"bread", 2, "pcs"}, {"cheese", 60, "g"}, {"butter", 10, "g"}}},

                {"Veggie Omelette",
                        "1. Beat the eggs with salt.\n2. Fry the chopped onion, tomato and bell pepper until soft.\n" +
                                "3. Pour the egg over the vegetables and cook until set.",
                        new Object[][]{{"egg", 3, "pcs"}, {"onion", 1, "pcs"}, {"tomato", 1, "pcs"}, {"bell pepper", 1, "pcs"}, {"salt", 1, "tsp"}}},

                {"Beef Stir-Fry",
                        "1. Slice the beef thinly.\n2. Stir-fry the beef and garlic in a hot pan until browned.\n" +
                                "3. Add the onion and bell pepper.\n4. Stir in the soy sauce and cook until the vegetables are tender.",
                        new Object[][]{{"beef", 200, "g"}, {"onion", 1, "pcs"}, {"bell pepper", 1, "pcs"}, {"soy sauce", 2, "tbsp"}, {"garlic", 2, "pcs"}}},

                {"Lentil Soup",
                        "1. Fry the onion and garlic until soft.\n2. Add the lentils, carrot and water.\n" +
                                "3. Simmer for 25-30 minutes until the lentils are soft, then serve.",
                        new Object[][]{{"lentil", 200, "g"}, {"onion", 1, "pcs"}, {"carrot", 1, "pcs"}, {"garlic", 2, "pcs"}, {"water", 750, "ml"}}},

                {"Tuna Salad",
                        "1. Drain the tuna and flake it into a bowl.\n2. Finely chop the onion and add it, with the lemon juice.\n" +
                                "3. Stir in the mayonnaise and serve.",
                        new Object[][]{{"tuna", 150, "g"}, {"onion", 1, "pcs"}, {"mayonnaise", 2, "tbsp"}, {"lemon", 1, "pcs"}}},

                {"Fruit Salad",
                        "1. Chop the banana, apple and orange into bite-sized pieces.\n2. Combine in a bowl.\n" +
                                "3. Drizzle with honey and toss gently before serving.",
                        new Object[][]{{"banana", 1, "pcs"}, {"apple", 1, "pcs"}, {"orange", 1, "pcs"}, {"honey", 1, "tbsp"}}},

                {"Garlic Butter Rice",
                        "1. Cook the rice.\n2. Melt the butter and gently fry the chopped garlic until fragrant.\n" +
                                "3. Stir the garlic butter and salt through the cooked rice and serve.",
                        new Object[][]{{"rice", 2, "cup"}, {"garlic", 3, "pcs"}, {"butter", 20, "g"}, {"salt", 1, "tsp"}}},

                {"Scrambled Eggs & Spinach",
                        "1. Melt the butter in a pan and wilt the spinach.\n2. Beat the eggs with salt and pour over the spinach.\n" +
                                "3. Stir gently over low heat until softly scrambled.",
                        new Object[][]{{"egg", 3, "pcs"}, {"spinach", 100, "g"}, {"butter", 10, "g"}, {"salt", 1, "tsp"}}},

                {"Chickpea Curry",
                        "1. Fry the onion and garlic until soft.\n2. Stir in the curry powder and cook briefly.\n" +
                                "3. Add the tomato and chickpeas and simmer for 15 minutes.",
                        new Object[][]{{"chickpea", 400, "g"}, {"onion", 1, "pcs"}, {"tomato", 2, "pcs"}, {"garlic", 2, "pcs"}, {"curry powder", 1, "tbsp"}}},

                {"Mashed Potatoes",
                        "1. Boil the potato until soft, then drain.\n2. Mash with the butter and milk.\n" +
                                "3. Season with salt and serve.",
                        new Object[][]{{"potato", 4, "pcs"}, {"milk", 100, "ml"}, {"butter", 20, "g"}, {"salt", 1, "tsp"}}},

                {"Carrot & Ginger Soup",
                        "1. Fry the onion and ginger until fragrant.\n2. Add the carrot and water.\n" +
                                "3. Simmer until the carrot is soft, then blend until smooth.",
                        new Object[][]{{"carrot", 4, "pcs"}, {"ginger", 1, "pcs"}, {"onion", 1, "pcs"}, {"water", 500, "ml"}}},

                {"Egg Fried Noodles",
                        "1. Cook the noodles and drain.\n2. Scramble the eggs in a hot pan and set aside.\n" +
                                "3. Stir-fry the carrot, then add the noodles, egg and soy sauce and toss together.",
                        new Object[][]{{"noodles", 200, "g"}, {"egg", 2, "pcs"}, {"carrot", 1, "pcs"}, {"soy sauce", 2, "tbsp"}}},
        };

        db.beginTransaction();
        try {
            for (Object[] recipe : recipes) {
                ContentValues recipeValues = new ContentValues();
                recipeValues.put("name", (String) recipe[0]);
                recipeValues.put("steps", (String) recipe[1]);
                long recipeId = db.insert(TABLE_RECIPE, null, recipeValues);

                Object[][] ingredients = (Object[][]) recipe[2];
                for (Object[] ingredient : ingredients) {
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put("recipe_id", recipeId);
                    ingredientValues.put("ingredient_name", (String) ingredient[0]);
                    ingredientValues.put("quantity", ((Number) ingredient[1]).doubleValue());
                    ingredientValues.put("unit", (String) ingredient[2]);
                    db.insert(TABLE_RECIPE_INGREDIENT, null, ingredientValues);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
