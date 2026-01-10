package com.example.greencircle;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.greencircle.databinding.FragmentPlantsBinding; // Auto-generated
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlantsFragment extends Fragment {

    private FragmentPlantsBinding binding;
    private PlantAdapter adapter;
    private List<Plant> fullPlantList;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate using Binding
        binding = FragmentPlantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        fullPlantList = new ArrayList<>();

        // 3. Setup RecyclerView (Access views via binding.viewId)
        binding.rvPlants.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlantAdapter(getContext(), new ArrayList<>());
        binding.rvPlants.setAdapter(adapter);

        // Fetch Data
        fetchPlants();

        // Setup Filter Listeners
        setupFilters();

        // Example: Floating Action Button Listener
        binding.fabAdd.setOnClickListener(v -> {
            showAddPlantDialog();
        });
    }

    private void fetchPlants() {
        db.collection("plants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullPlantList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Plant plant = doc.toObject(Plant.class);
                        if (plant != null) {
                            plant.setPlantId(doc.getId());
                            fullPlantList.add(plant);
                        }
                    }
                    adapter.updateList(fullPlantList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading plants", Toast.LENGTH_SHORT).show();
                    Log.e("PlantsFragment", "Error fetching", e);
                });
    }

    private void setupFilters() {
        // Chip Group Listener
        // Ensure your XML ChipGroup has id: android:id="@+id/filterChipGroup"
        binding.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterList();
        });

        // Search Bar Listener
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList() {
        String query = binding.etSearch.getText().toString().toLowerCase().trim();

        // Get selected Chip Text
        String selectedCategory = "All";
        int checkedId = binding.filterChipGroup.getCheckedChipId();

        if (checkedId != View.NO_ID) {
            Chip chip = binding.getRoot().findViewById(checkedId);
            if (chip != null) {
                selectedCategory = chip.getText().toString();
            }
        }

        List<Plant> filtered = new ArrayList<>();

        for (Plant p : fullPlantList) {
            // Search Logic
            boolean matchesSearch = p.getCommonName().toLowerCase().contains(query);

            // Chip Logic (Example: Filtering by Sunlight)
            boolean matchesCategory = selectedCategory.equals("All");

            if (!matchesCategory) {
                // Check if the plant's sunlight field matches the selected chip (e.g., "Full Sun")
                // Adjust "getSunlight()" if you are filtering by a different field
                if (p.getSunlight() != null && p.getSunlight().equalsIgnoreCase(selectedCategory)) {
                    matchesCategory = true;
                }
            }

            if (matchesSearch && matchesCategory) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear binding to prevent memory leaks
    }
    private void showAddPlantDialog() {
        // 1. Inflate the Dialog Layout using AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_plant, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();

        // Make background transparent for rounded corners effect (optional)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        // 2. Initialize Dialog Views
        EditText etName = dialogView.findViewById(R.id.etCommonName);
        EditText etScientific = dialogView.findViewById(R.id.etScientificName);
        EditText etSunlight = dialogView.findViewById(R.id.etSunlight);
        EditText etSeason = dialogView.findViewById(R.id.etSeason);
        EditText etWater = dialogView.findViewById(R.id.etWaterFreq);
        EditText etGrowth = dialogView.findViewById(R.id.etGrowthDuration);
        EditText etImage = dialogView.findViewById(R.id.etImageUrl);
        android.widget.Button btnSubmit = dialogView.findViewById(R.id.btnSubmitPlant);

        // 3. Handle Submit Click
        btnSubmit.setOnClickListener(v -> {
            // Get Inputs
            String name = etName.getText().toString().trim();
            String scientific = etScientific.getText().toString().trim();
            String sunlight = etSunlight.getText().toString().trim();
            String season = etSeason.getText().toString().trim();
            String waterStr = etWater.getText().toString().trim();
            String growthStr = etGrowth.getText().toString().trim();
            String imageUrl = etImage.getText().toString().trim();

            // Basic Validation
            if (name.isEmpty() || waterStr.isEmpty() || growthStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse Numbers
            int waterFreq = Integer.parseInt(waterStr);
            int growthDays = Integer.parseInt(growthStr);

            // 4. Create Plant Object
            // Using a Map is often safer for dynamic adds, or use your Plant class setters
            // Option A: Using your Plant class (Preferred)
            Plant newPlant = new Plant();
            newPlant.setCommonName(name);
            newPlant.setScientificName(scientific);
            newPlant.setSunlight(sunlight);
            newPlant.setSeason(season);
            newPlant.setWaterFrequencyDays(waterFreq); // Ensure your Plant.java has this setter
            newPlant.setGrowthDurationDays(growthDays); // Ensure your Plant.java has this setter
            newPlant.setImageUrl(imageUrl);

            // 5. Upload to Firestore
            db.collection("plants")
                    .add(newPlant)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Plant Added Successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchPlants(); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add plant: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}