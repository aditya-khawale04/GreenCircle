package com.example.greencircle;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.greencircle.databinding.ActivityMainBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Setup Bottom Navigation
        setupBottomNavigation();

        // 2. Load Default Fragment (Home)
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // 3. AI Mentor Button Logic
        binding.btnAI.setOnClickListener(v -> {
            GardenMentorBottomSheet bottomSheet = new GardenMentorBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "GardenMentorAI");
        });
    }

    private void setupBottomNavigation() {
        binding.navMenuView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // NOTE: Ensure these IDs match your res/menu/bottom_nav_menu.xml
            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.planner) {
                selectedFragment = new PlannerFragment();
            } else if (itemId == R.id.plants) {
                selectedFragment = new PlantsFragment();
            } else if (itemId == R.id.community) {
                 selectedFragment = new CommunityFragment();
            } else if (itemId == R.id.journal) {
                 selectedFragment = new JournalFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    // --- REUSED ADD TASK LOGIC (Moved from HomeFragment) ---
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        // Initialize Views
        EditText etPlantId = dialogView.findViewById(R.id.etGardenPlantId);
        RadioGroup rgType = dialogView.findViewById(R.id.rgTaskType);
        TextView tvDate = dialogView.findViewById(R.id.tvSelectedDate);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnSave = dialogView.findViewById(R.id.btnSaveTask);

        final Calendar calendar = Calendar.getInstance();

        // Date Picker
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        tvDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Save Button
        btnSave.setOnClickListener(v -> {
            String plantId = etPlantId.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();

            if (plantId.isEmpty()) {
                etPlantId.setError("Plant ID required");
                return;
            }
            if (user == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgType.getCheckedRadioButtonId();
            RadioButton selectedRb = dialogView.findViewById(selectedId);
            String taskType = selectedRb.getText().toString();

            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("userId", user.getUid());
            taskMap.put("gardenPlantId", plantId);
            taskMap.put("taskType", taskType);
            taskMap.put("dueTime", new Timestamp(calendar.getTime()));
            taskMap.put("isCompleted", false);

            db.collection("tasks")
                    .add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Task Added Successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Optional: If current fragment is Home, you might want to refresh it
                        refreshHomeIfVisible();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void refreshHomeIfVisible() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (currentFragment instanceof HomeFragment) {
            // Re-load the fragment to see new data
            replaceFragment(new HomeFragment());
        }
    }
}