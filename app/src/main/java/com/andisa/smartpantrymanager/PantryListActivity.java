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

import com.andisa.smartpantrymanager.adapter.PantryAdapter;
import com.andisa.smartpantrymanager.data.PantryDataSource;
import com.andisa.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;

/**
 * The launcher screen: shows every ingredient currently in the
 * pantry using a RecyclerView bound to the database (Section 2.2),
 * and lets the user add, edit or delete items.
 *
 * Like ContactListActivity in the module, the list is (re)loaded in
 * onResume() rather than onCreate() so that changes made on the
 * Add/Edit screen are reflected as soon as the user returns here.
 */
public class PantryListActivity extends AppCompatActivity {

    private RecyclerView recyclerPantry;
    private TextView textEmptyPantry;
    private PantryAdapter pantryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);

        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        pantryAdapter = new PantryAdapter(new ArrayList<>(), this, this::openEditScreen);
        recyclerPantry.setAdapter(pantryAdapter);

        ImageButton buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        initPantryButton();
        initRecipesButton();
        initSettingsButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        PantryDataSource ds = new PantryDataSource(this);
        try {
            ds.open();
            ArrayList<PantryItem> items = ds.getAllPantryItems();
            ds.close();
            pantryAdapter.setData(items);
            textEmptyPantry.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading pantry", Toast.LENGTH_LONG).show();
        }
    }

    private void openEditScreen(PantryItem item) {
        Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
        intent.putExtra("pantryItemId", item.getId());
        startActivity(intent);
    }

    // -----------------------------------------------------------
    // Bottom navigation bar - same pattern as the module's navbar:
    // one init method per button, current screen's button disabled.
    // -----------------------------------------------------------

    private void initPantryButton() {
        ImageButton ib = findViewById(R.id.imageButtonPantry);
        ib.setEnabled(false);
    }

    private void initRecipesButton() {
        ImageButton ib = findViewById(R.id.imageButtonRecipes);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, SuggestedRecipesActivity.class);
            startActivity(intent);
        });
    }

    private void initSettingsButton() {
        ImageButton ib = findViewById(R.id.imageButtonSettings);
        ib.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
