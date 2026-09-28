package com.andisa.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Settings screen - stores its two preferences (expiring-soon
 * alerts, preferred unit system) using SharedPreferences, exactly
 * as ContactSettingsActivity does in the module: each control's
 * listener writes straight to the SharedPreferences object as soon
 * as the user changes it.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SmartPantryPrefs";

    private Switch switchExpiryAlerts;
    private RadioButton radioMetric;
    private RadioButton radioCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        radioMetric = findViewById(R.id.radioMetric);
        radioCount = findViewById(R.id.radioCount);

        initSettingsValues();
        initExpiryAlertsSwitch();
        initUnitsRadioGroup();

        initPantryButton();
        initRecipesButton();
        initSettingsButton();
    }

    /** Reads the stored preferences and sets the controls to match, like initSettings() in the module. */
    private void initSettingsValues() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alertsEnabled = prefs.getBoolean("expiry_alerts_enabled", true);
        switchExpiryAlerts.setChecked(alertsEnabled);

        String unitsPreference = prefs.getString("units_preference", "metric");
        if ("metric".equals(unitsPreference)) {
            radioMetric.setChecked(true);
        } else {
            radioCount.setChecked(true);
        }
    }

    private void initExpiryAlertsSwitch() {
        switchExpiryAlerts.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("expiry_alerts_enabled", isChecked);
            editor.apply();
        });
    }

    private void initUnitsRadioGroup() {
        RadioGroup radioGroupUnits = findViewById(R.id.radioGroupUnits);
        radioGroupUnits.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            String value = (checkedId == R.id.radioMetric) ? "metric" : "count";
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("units_preference", value);
            editor.apply();
        });
    }

    // -----------------------------------------------------------
    // Bottom navigation bar
    // -----------------------------------------------------------

    private void initPantryButton() {
        ImageButton ib = findViewById(R.id.imageButtonPantry);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PantryListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initRecipesButton() {
        ImageButton ib = findViewById(R.id.imageButtonRecipes);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SuggestedRecipesActivity.class);
            startActivity(intent);
        });
    }

    private void initSettingsButton() {
        ImageButton ib = findViewById(R.id.imageButtonSettings);
        ib.setEnabled(false);
    }
}
