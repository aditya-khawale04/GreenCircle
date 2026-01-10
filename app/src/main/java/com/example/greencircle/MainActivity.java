package com.example.greencircle;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.greencircle.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navMenuView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.planner) {
                replaceFragment(new PlannerFragment());
                return true;
            } else if (itemId == R.id.plants) {
                replaceFragment(new PlantsFragment());
                return true;
            } else if (itemId == R.id.community) {
                replaceFragment(new CommunityFragment());
                return true;
            } else if (itemId == R.id.journal) {
                replaceFragment(new JournalFragment());
                return true;
            }
            return false;
        });

        // Load the initial fragment when the activity starts
        if (savedInstanceState == null) {
            binding.navMenuView.setSelectedItemId(R.id.home); // Select Home by default
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment) // Finish the replace call with the container ID and the commit
                .commit();
    }
}