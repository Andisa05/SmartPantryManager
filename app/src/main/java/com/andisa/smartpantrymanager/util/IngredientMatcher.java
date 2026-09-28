package com.andisa.smartpantrymanager.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the "reasonably robust" matching described in the brief
 * (Section 2.3): normalising ingredient names (case, whitespace,
 * simple English plurals) and converting units to a common base
 * unit so that "2kg" and "2000g" - or "1 tbsp" and "15 ml" - are
 * recognised as the same amount.
 *
 * This is deliberately NOT a full NLP solution (the brief says that
 * isn't required). It solves the specific problem the brief calls
 * out: a naive exact-string match breaking on trivial differences
 * such as "tomato" vs "tomatoes".
 */
public final class IngredientMatcher {

    private IngredientMatcher() {
        // utility class - no instances
    }

    /**
     * Normalises an ingredient name for comparison: trims
     * whitespace, lower-cases it, and strips a simple English
     * plural suffix so "Tomatoes" and "tomato" resolve to the same
     * key.
     */
    public static String normalizeName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String s = rawName.trim().toLowerCase();
        s = s.replaceAll("\\s+", " ");
        return singularize(s);
    }

    private static String singularize(String s) {
        if (s.endsWith("ies") && s.length() > 3) {
            // e.g. "berries" -> "berry"
            return s.substring(0, s.length() - 3) + "y";
        }
        if ((s.endsWith("oes") || s.endsWith("shes") || s.endsWith("ches")
                || s.endsWith("xes") || s.endsWith("sses")) && s.length() > 3) {
            // e.g. "tomatoes" -> "tomato", "dishes" -> "dish"
            return s.substring(0, s.length() - 2);
        }
        if (s.endsWith("s") && !s.endsWith("ss") && s.length() > 2) {
            // e.g. "onions" -> "onion", "eggs" -> "egg"
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** Unit categories that can be converted between one another. */
    public enum UnitCategory { MASS, VOLUME, COUNT, UNKNOWN }

    private static final Map<String, String> UNIT_SYNONYMS = new HashMap<>();
    private static final Map<String, UnitCategory> UNIT_CATEGORY = new HashMap<>();
    // How many of the category's base unit one unit represents.
    // Base unit for MASS is grams, for VOLUME is millilitres.
    private static final Map<String, Double> UNIT_TO_BASE = new HashMap<>();

    static {
        addUnit("g", UnitCategory.MASS, 1.0, "g", "gram", "grams", "gr");
        addUnit("kg", UnitCategory.MASS, 1000.0, "kg", "kilogram", "kilograms");

        addUnit("ml", UnitCategory.VOLUME, 1.0, "ml", "milliliter", "millilitre", "milliliters", "millilitres");
        addUnit("l", UnitCategory.VOLUME, 1000.0, "l", "liter", "litre", "liters", "litres");
        addUnit("tsp", UnitCategory.VOLUME, 5.0, "tsp", "teaspoon", "teaspoons");
        addUnit("tbsp", UnitCategory.VOLUME, 15.0, "tbsp", "tablespoon", "tablespoons");
        addUnit("cup", UnitCategory.VOLUME, 240.0, "cup", "cups");

        addUnit("pcs", UnitCategory.COUNT, 1.0, "pcs", "pc", "piece", "pieces", "unit", "units", "");
    }

    private static void addUnit(String canonical, UnitCategory category, double toBase, String... synonyms) {
        UNIT_CATEGORY.put(canonical, category);
        UNIT_TO_BASE.put(canonical, toBase);
        for (String syn : synonyms) {
            UNIT_SYNONYMS.put(syn, canonical);
        }
    }

    /** Normalises a unit string (case/whitespace/synonym) to its canonical form, e.g. "Grams" -> "g". */
    public static String normalizeUnit(String rawUnit) {
        if (rawUnit == null) {
            return "pcs";
        }
        String s = rawUnit.trim().toLowerCase();
        String canonical = UNIT_SYNONYMS.get(s);
        return canonical != null ? canonical : s;
    }

    public static UnitCategory categoryOf(String canonicalUnit) {
        UnitCategory c = UNIT_CATEGORY.get(canonicalUnit);
        return c != null ? c : UnitCategory.UNKNOWN;
    }

    /**
     * Converts a quantity to its category's base unit (grams for
     * mass, millilitres for volume, "pcs" for count). Returns
     * Double.NaN if the unit is unrecognised, since it then cannot
     * be safely compared to anything.
     */
    public static double toBaseQuantity(double quantity, String canonicalUnit) {
        Double factor = UNIT_TO_BASE.get(canonicalUnit);
        if (factor == null) {
            return Double.NaN;
        }
        return quantity * factor;
    }
}
