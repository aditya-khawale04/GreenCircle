package com.example.greencircle;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.greencircle.databinding.FragmentCommunityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private FragmentCommunityBinding binding;
    private CommunityAdapter adapter;
    private List<CommunityPost> postList;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();

        // Setup RecyclerView
        binding.rvCommunityPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommunityAdapter(getContext(), postList);
        binding.rvCommunityPosts.setAdapter(adapter);

        // Fetch Posts
        fetchPosts();

        binding.btnNewPost.setOnClickListener(v -> showNewPostDialog());
    }

    private void showNewPostDialog() {
        // 1. Inflate Layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_post, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        // 2. Initialize Views
        EditText etContent = dialogView.findViewById(R.id.etPostContent);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitPost);

        // 3. Submit Logic
        btnSubmit.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Validation
            if (content.isEmpty()) {
                etContent.setError("Please write something!");
                return;
            }
            if (user == null) {
                Toast.makeText(getContext(), "You must be logged in to post", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prepare Data
            String userName = (user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                    ? user.getDisplayName()
                    : "Community Member";

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("userId", user.getUid());
            postMap.put("userName", userName);
            postMap.put("role", "Gardener"); // Default role
            postMap.put("content", content);
            postMap.put("imageUrl", imageUrl);
            postMap.put("likes", 0);
            postMap.put("createdAt", FieldValue.serverTimestamp());

            // Upload to Firestore
            db.collection("community_posts")
                    .add(postMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Posted successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchPosts(); // Refresh feed
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void fetchPosts() {
        db.collection("community_posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CommunityPost post = doc.toObject(CommunityPost.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            postList.add(post);
                        }
                    }
                    adapter.updateList(postList);
                })
                .addOnFailureListener(e -> {
                    Log.e("CommunityFragment", "Error loading posts", e);
                    Toast.makeText(getContext(), "Failed to load community feed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}