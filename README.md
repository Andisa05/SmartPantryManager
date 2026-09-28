# Smart Pantry Manager

A Java Android application built for the **Mobile App Development 700** practical
assignment at Richfield Graduate Institute of Technology.

## What it does

Smart Pantry Manager helps a user reduce food waste by tracking the ingredients
they actually have at home (their "pantry") and suggesting recipes they can cook
using **strictly** those ingredients - no shopping trip required. A recipe is only
suggested if every single ingredient it needs is already in the pantry, in at
least the required quantity. Recipes missing exactly one ingredient are shown
separately in an "Almost There" list.

## Screens

1. **Pantry List** (launcher) - every ingredient currently in the pantry, in a
   RecyclerView bound to the database. Tap an item to edit it, tap the delete
   button to remove it, tap + to add a new one.
2. **Add / Edit Ingredient** - name, quantity, unit and an optional expiry date
   (via a DatePickerDialog). Validates that a name and a positive numeric
   quantity have been entered before saving.
3. **Suggested Recipes** - runs the strict-matching rule against the current
   pantry and lists only the recipes that can be made right now, plus a
   separate "Almost There" bonus list for recipes missing one ingredient.
4. **Recipe Detail** - full ingredient list and preparation steps for a
   selected recipe.
5. **Settings** - toggle for expiring-soon alerts on the pantry list, and a
   preferred unit system (metric / count) used as the default for new items.

## Database

**SQLite**, via `SQLiteOpenHelper` (`PantryDBHelper`) and a plain data-source
class (`PantryDataSource`) that opens/closes the database and runs the SQL
queries - the exact pattern taught in the module's persistent-data chapter
(the `ContactDBHelper` / `ContactDataSource` example). No Room, Firebase or
external backend is used.

This was chosen over Firebase or PostgreSQL because it:
- matches the approach covered in class, so every line of it can be explained;
- needs no account, network connection, or hosted backend - the app works
  fully offline, which makes it simpler and more reliable to demonstrate; and
- is the natural fit for data that is genuinely private to one device/user
  (a personal pantry), rather than something that needs to sync or be shared.

Three tables are used:
- `pantry_item` - the user's ingredients (name, quantity, unit, expiry date).
- `recipe` - the seeded recipe collection (name, preparation steps).
- `recipe_ingredient` - each recipe's required ingredients (name, quantity, unit),
  linked to `recipe` by `recipe_id`.

20 recipes are pre-loaded into `recipe` / `recipe_ingredient` the first time the
app runs (see `PantryDBHelper.seedRecipes()`).

Full CRUD is implemented on pantry items (Create/Read/Update/Delete), and the
data persists after the app is closed and reopened, since it lives in the
on-device SQLite file rather than in memory.

## The strict-matching rule

Implemented in `IngredientMatcher` (name/unit normalisation) and
`PantryDataSource.getSuggestedRecipes()` (the matching itself):

- Ingredient names are lower-cased, trimmed, and run through a small
  singularizer (`tomatoes` -> `tomato`, `onions` -> `onion`, `berries` -> `berry`)
  so that simple plural/singular differences don't break a match.
- Units are normalised to a canonical form (`Grams`, `gram`, `g` all become `g`)
  and converted to a common base unit within their category - grams for mass,
  millilitres for volume (using standard cooking conversions: 1 tsp = 5 ml,
  1 tbsp = 15 ml, 1 cup = 240 ml) - so "2 kg" correctly satisfies a recipe that
  needs "2000 g".
- A recipe is only added to the Suggested Recipes list if **every** ingredient
  it needs is satisfied. A recipe missing exactly one ingredient is added to
  the separate "Almost There" list instead.

## Project structure

```
app/src/main/java/com/andisa/smartpantrymanager/
  model/      PantryItem, Recipe, RecipeIngredient
  data/       PantryDBHelper (SQLiteOpenHelper), PantryDataSource
  util/       IngredientMatcher (name/unit normalisation + strict matching helpers)
  adapter/    PantryAdapter, RecipeAdapter (RecyclerView.Adapter + ViewHolder)
  *.java      PantryListActivity, AddEditIngredientActivity,
              SuggestedRecipesActivity, RecipeDetailActivity, SettingsActivity
```

## Setup / run instructions

1. Open the project folder in **Android Studio** (Giraffe or newer recommended)
   and let Gradle sync - it will download the Android Gradle Plugin and the
   AndroidX/Material/RecyclerView dependencies listed in `app/build.gradle`.
2. If prompted, install/select an Android SDK platform for API 34 (compileSdk)
   - the app itself supports devices from API 21 upward (minSdk).
3. Run the app on an emulator or a physical device (`Run > Run 'app'`).
   `PantryListActivity` is the launcher activity.
4. On first run the database is created and pre-loaded with 20 recipes
   automatically - no seed data needs to be entered manually.
5. To see the strict-matching rule in action: add ingredients matching one of
   the seeded recipes (see `PantryDBHelper.seedRecipes()` for the full list,
   e.g. 3 pcs egg + 100 g spinach + 10 g butter + 1 tsp salt for
   "Scrambled Eggs & Spinach"), open **Suggested Recipes**, then remove one of
   those ingredients and pull the list up again to watch the recipe disappear.

## Out of scope

No Google Maps, mapping SDK, or device location/GPS services are used
anywhere in this app, per the assignment brief.
