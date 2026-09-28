package com.andisa.smartpantrymanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.andisa.smartpantrymanager.data.PantryDataSource;
import com.andisa.smartpantrymanager.model.PantryItem;

import java.util.Calendar;

/**
 * Add/Edit Ingredient screen. Used for both creating a new pantry
 * item and editing an existing one, exactly like MainActivity in
 * the module handles both add and edit for a Contact: the screen
 * is opened either with no extra (new item, currentItem.getId()
 * stays -1) or with a "pantryItemId" extra (existing item, loaded
 * and updated instead of inserted).
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editQuantity;
    private Spinner spinnerUnit;
    private TextView textExpiryDate;

    private PantryItem currentItem;
    private long selectedExpiryMillis = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        textExpiryDate = findViewById(R.id.textExpiryDate);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.unit_options, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        Button buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickDate.setOnClickListener(v -> showDatePicker());

        Button buttonSaveItem = findViewById(R.id.buttonSaveItem);
        buttonSaveItem.setOnClickListener(v -> saveItem());

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("pantryItemId")) {
            loadExistingItem(extras.getInt("pantryItemId"));
        } else {
            currentItem = new PantryItem();
            applyDefaultUnitPreference();
        }

        initPantryButton();
        initRecipesButton();
        initSettingsButton();
    }

    private void loadExistingItem(int id) {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            currentItem = ds.getPantryItem(id);
            ds.close();
        } catch (Exception e) {
            Toast.makeText(this, "Could not load item", Toast.LENGTH_LONG).show();
            currentItem = new PantryItem();
        }

        editName.setText(currentItem.getName());
        editQuantity.setText(trimTrailingZero(currentItem.getQuantity()));
        setSpinnerToUnit(currentItem.getUnit());

        if (currentItem.hasExpiryDate()) {
            selectedExpiryMillis = currentItem.getExpiryDate();
            textExpiryDate.setText(DateFormat.format("yyyy/MM/dd", selectedExpiryMillis));
        }
    }

    /** New items default to the unit system chosen on the Settings screen. */
    private void applyDefaultUnitPreference() {
        SharedPreferences prefs = getSharedPreferences("SmartPantryPrefs", MODE_PRIVATE);
        String preference = prefs.getString("units_preference", "metric");
        setSpinnerToUnit("metric".equals(preference) ? "g" : "pcs");
    }

    private void setSpinnerToUnit(String unit) {
        if (unit == null) {
            return;
        }
        ArrayAdapter adapter = (ArrayAdapter) spinnerUnit.getAdapter();
        int position = adapter.getPosition(unit);
        if (position >= 0) {
            spinnerUnit.setSelection(position);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedExpiryMillis > 0) {
            calendar.setTimeInMillis(selectedExpiryMillis);
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, 0, 0, 0);
            selectedExpiryMillis = picked.getTimeInMillis();
            textExpiryDate.setText(DateFormat.format("yyyy/MM/dd", selectedExpiryMillis));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /**
     * Validates the form (Section 3.1 requires input validation on
     * any data entry form) and, if valid, inserts or updates the
     * pantry item.
     */
    private void saveItem() {
        String name = editName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Ingredient name is required");
            editName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            editQuantity.setError("Quantity is required");
            editQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid number");
            editQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            editQuantity.setError("Quantity must be greater than zero");
            editQuantity.requestFocus();
            return;
        }

        currentItem.setName(name);
        currentItem.setQuantity(quantity);
        currentItem.setUnit((String) spinnerUnit.getSelectedItem());
        currentItem.setExpiryDate(selectedExpiryMillis);

        PantryDataSource ds = new PantryDataSource(this);
        boolean wasSuccessful;
        try {
            ds.open();
            if (currentItem.getId() == -1) {
                wasSuccessful = ds.insertPantryItem(currentItem);
            } else {
                wasSuccessful = ds.updatePantryItem(currentItem);
            }
            ds.close();
        } catch (Exception e) {
            wasSuccessful = false;
        }

        if (wasSuccessful) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save item", Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(AddEditIngredientActivity.this, PantryListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initRecipesButton() {
        ImageButton ib = findViewById(R.id.imageButtonRecipes);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(AddEditIngredientActivity.this, SuggestedRecipesActivity.class);
            startActivity(intent);
        });
    }

    private void initSettingsButton() {
        ImageButton ib = findViewById(R.id.imageButtonSettings);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(AddEditIngredientActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
