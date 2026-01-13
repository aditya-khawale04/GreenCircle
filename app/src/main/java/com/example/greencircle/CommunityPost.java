package com.example.greencircle;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CommunityPost {
    private String postId;
    private String userId;
    private String userName;
    private String role; // e.g. "Mentor"
    private String content;
    private String imageUrl;
    private int likes;
    private Timestamp createdAt;

    public CommunityPost() {} // Empty constructor for Firestore

    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getLikes() { return likes; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Helper for Date
    public String getFormattedDate() {
        if (createdAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
        return sdf.format(createdAt.toDate());
    }
}