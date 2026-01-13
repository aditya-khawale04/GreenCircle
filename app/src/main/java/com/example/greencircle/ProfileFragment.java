package com.example.greencircle;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail;
    private Spinner spinnerRole;
    private ImageView imgProfile;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String[] roles = {"Beginner", "Intermediate", "Mentor", "Expert"};

    // Variable to hold the new image URI temporarily
    private Uri selectedImageUri = null;

    // 1. Define the Image Picker Launcher
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Show the selected image immediately (Optimistic UI)
                    imgProfile.setImageURI(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        spinnerRole = view.findViewById(R.id.spinnerRole);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance(); // Init Storage

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(adapter);

        loadUserData();

        // 2. Click Listener to Launch Gallery
        imgProfile.setOnClickListener(v -> {
            // "image/*" filters for image files only
            pickImageLauncher.launch("image/*");
        });

        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            etName.setText(user.getDisplayName());
            etEmail.setText(user.getEmail());

            // Load current profile image
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(imgProfile);
            }

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null) setSpinnerToValue(spinnerRole, role);

                            // Prefer Firestore name if available
                            String fsName = documentSnapshot.getString("name");
                            if (fsName != null) etName.setText(fsName);

                            // Prefer Firestore image if available (sometimes updates faster than Auth)
                            String fsImage = documentSnapshot.getString("photoUrl");
                            if (fsImage != null && !fsImage.isEmpty()) {
                                Glide.with(this).load(fsImage).into(imgProfile);
                            }
                        }
                    });
        }
    }

    private void saveProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String newName = etName.getText().toString().trim();
        if (TextUtils.isEmpty(newName)) {
            etName.setError("Name required");
            return;
        }

        btnSave.setEnabled(false); // Prevent double clicks
        Toast.makeText(getContext(), "Saving...", Toast.LENGTH_SHORT).show();

        // Check if we need to upload a new image first
        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(user, newName);
        } else {
            // Just update text details
            updateProfileDetails(user, newName, null);
        }
    }

    // 3. Upload Image to Firebase Storage
    private void uploadImageAndSaveProfile(FirebaseUser user, String newName) {
        // Create a reference: profile_images/USER_ID.jpg
        StorageReference profileRef = storage.getReference()
                .child("profile_images/" + user.getUid() + ".jpg");

        profileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the Download URL
                    profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateProfileDetails(user, newName, downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image Upload Failed", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
    }

    // 4. Update Firestore & Auth
    private void updateProfileDetails(FirebaseUser user, String newName, @Nullable String newPhotoUrl) {
        String newEmail = etEmail.getText().toString().trim();
        String newRole = spinnerRole.getSelectedItem().toString();

        // A. Prepare Auth Updates
        UserProfileChangeRequest.Builder authUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        if (newPhotoUrl != null) {
            authUpdates.setPhotoUri(Uri.parse(newPhotoUrl));
        }

        user.updateProfile(authUpdates.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // B. Prepare Firestore Updates
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", newName);
                userMap.put("email", newEmail);
                userMap.put("role", newRole);
                if (newPhotoUrl != null) {
                    userMap.put("photoUrl", newPhotoUrl);
                }

                db.collection("users").document(user.getUid())
                        .set(userMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                            btnSave.setEnabled(true);
                            // Optional: Go back
                            // getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Database Update Failed", Toast.LENGTH_SHORT).show();
                            btnSave.setEnabled(true);
                        });
            } else {
                Toast.makeText(getContext(), "Auth Update Failed", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            }
        });
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}