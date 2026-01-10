package com.example.greencircle;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.greencircle.databinding.ActivitySignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivitySignUpBinding binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Input Data
                String name = binding.etName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String pass = binding.etPass.getText().toString().trim();

                // Get Selected Role
                int checkedId = binding.toggleGroupUserType.getCheckedButtonId();
                String role = "";

                if (checkedId == binding.btnBeginner.getId()) {
                    role = "Beginner";
                } else if (checkedId == binding.btnMentor.getId()) {
                    role = "Mentor";
                }

                //  Validation
                if (name.isEmpty()) {
                    binding.etName.setError("Name is required");
                    return;
                }
                if (email.isEmpty()) {
                    binding.etEmail.setError("Email is required");
                    return;
                }
                if (pass.length() < 6) {
                    binding.etPass.setError("Password must be at least 6 chars");
                    return;
                }
                if (role.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please select a role (Beginner or Mentor)", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create User in Auth
                String finalRole = role; // specific variable for inner class usage

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // ... (Your existing success code: saving to Firestore) ...
                                } else {
                                    // ERROR HANDLING
                                    try {
                                        throw task.getException();
                                    }
                                    //  Check if user already exists
                                    catch (FirebaseAuthUserCollisionException e) {
                                        binding.etEmail.setError("User with this email already exists");
                                        binding.etEmail.requestFocus();
                                        Toast.makeText(SignUpActivity.this, "Email already registered. Please Login.", Toast.LENGTH_LONG).show();
                                    }
                                    // Check if password is too weak (extra safety)
                                    catch (FirebaseAuthWeakPasswordException e) {
                                        binding.etPass.setError("Password is too weak");
                                        binding.etPass.requestFocus();
                                    }
                                    // Check for invalid email format
                                    catch (FirebaseAuthInvalidCredentialsException e) {
                                        binding.etEmail.setError("Invalid email format");
                                        binding.etEmail.requestFocus();
                                    }
                                    // General fallback
                                    catch (Exception e) {
                                        Log.e(TAG, "Sign up failed", e);
                                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
            }
        });
                    // to login activity
        binding.haveAccount.setOnClickListener(this::goToLoginActivity);
    }

    private void goToLoginActivity(View v) {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}