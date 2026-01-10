package com.example.greencircle;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.greencircle.databinding.FragmentHomeBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TaskAdapter taskAdapter;
    private List<Task> todayTaskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        todayTaskList = new ArrayList<>();

        // Setup RecyclerView with Toggle Listener
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        taskAdapter = new TaskAdapter(getContext(), todayTaskList, (task, isCompleted) -> {
            toggleTaskStatus(task, isCompleted);
        });

        binding.rvTasks.setAdapter(taskAdapter);

        fetchTasksForToday();
        fetchGreeting();

        binding.btnAddTask.setOnClickListener(v -> showAddTaskDialog());
    }
    private void fetchTasksForToday() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("tasks")
                    .whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        todayTaskList.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Task task = doc.toObject(Task.class);
                            if (task != null) {
                                task.setTaskId(doc.getId());
                                // Show only today's tasks
                                if (isToday(task)) {
                                    todayTaskList.add(task);
                                }
                            }
                        }
                        taskAdapter.updateList(todayTaskList);
                        updatePendingCount();
                    })
                    .addOnFailureListener(e -> Log.e("Home", "Error", e));
        }
    }

    private void toggleTaskStatus(Task task, boolean isCompleted) {
        if (task.getTaskId() == null) return;

        // 1. Update UI Count immediately
        updatePendingCount();

        // 2. Update Firestore with new state
        db.collection("tasks").document(task.getTaskId())
                .update("isCompleted", isCompleted)
                .addOnSuccessListener(aVoid -> {
                    // Success feedback
                })
                .addOnFailureListener(e -> {
                    // Revert if failed
                    task.setCompleted(!isCompleted);
                    taskAdapter.notifyDataSetChanged();
                    updatePendingCount();
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePendingCount() {
        int pendingCount = 0;
        for (Task t : todayTaskList) {
            if (!t.isCompleted()) {
                pendingCount++;
            }
        }
        binding.tvPendingTasksCount.setText(String.valueOf(pendingCount));
    }

    private boolean isToday(Task task) {
        if (task.getDueTime() == null) return false;
        return DateUtils.isToday(task.getDueTime().toDate().getTime());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void showAddTaskDialog() {
        // Inflate the Dialog Layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Optional: Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        //  Initialize Views from the Dialog Layout
        EditText etPlantId = dialogView.findViewById(R.id.etGardenPlantId);
        RadioGroup rgType = dialogView.findViewById(R.id.rgTaskType);
        TextView tvDate = dialogView.findViewById(R.id.tvSelectedDate);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnSave = dialogView.findViewById(R.id.btnSaveTask);

        // Variable to hold the selected date (Default is "Now")
        final Calendar calendar = Calendar.getInstance();

        // Date Picker Logic
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        // Update the text view to show selected date
                        tvDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            // Prevent selecting past dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Save Button Logic
        btnSave.setOnClickListener(v -> {
            String plantId = etPlantId.getText().toString().trim();
            FirebaseUser user = mAuth.getCurrentUser();

            // --- VALIDATION ---
            if (plantId.isEmpty()) {
                etPlantId.setError("Plant ID is required");
                return;
            }
            if (user == null) {
                Toast.makeText(getContext(), "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the selected Task Type text (Watering, Fertilizing, etc.)
            int selectedId = rgType.getCheckedRadioButtonId();
            RadioButton selectedRb = dialogView.findViewById(selectedId);
            String taskType = selectedRb.getText().toString();

            // 5. Prepare Data for Firestore
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("userId", user.getUid());
            taskMap.put("gardenPlantId", plantId);
            taskMap.put("taskType", taskType);
            taskMap.put("dueTime", new Timestamp(calendar.getTime())); // Convert Calendar to Firestore Timestamp
            taskMap.put("isCompleted", false);

            // 6. Upload to Firestore
            db.collection("tasks")
                    .add(taskMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Task Added Successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        // Refresh the list to show the new task immediately
                        fetchTasksForToday();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
    private void fetchGreeting() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "Gardener";
            binding.tvGreeting.setText("Hello, " + name + "!");
        }
    }
}