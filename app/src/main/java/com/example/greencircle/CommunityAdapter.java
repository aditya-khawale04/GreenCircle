package com.example.greencircle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.PostViewHolder> {

    private Context context;
    private List<CommunityPost> postList;

    public CommunityAdapter(Context context, List<CommunityPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    public void updateList(List<CommunityPost> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        holder.tvName.setText(post.getUserName());
        holder.tvDate.setText(post.getFormattedDate());
        holder.tvContent.setText(post.getContent());
        holder.tvLikes.setText(String.valueOf(post.getLikes()));

        // Role Badge Logic
        if (post.getRole() != null && post.getRole().equalsIgnoreCase("Mentor")) {
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.tvRole.setText("MENTOR");
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }

        // Image Logic
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.imgPost.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .centerCrop()
                    .into(holder.imgPost);
        } else {
            holder.imgPost.setVisibility(View.GONE);
        }

        // Avatar Placeholder (You'd usually load this from URL too)
        holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvContent, tvLikes, tvRole;
        ShapeableImageView imgPost, imgAvatar;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvDate = itemView.findViewById(R.id.tvPostDate);
            tvContent = itemView.findViewById(R.id.tvPostContent);
            tvLikes = itemView.findViewById(R.id.tvLikesCount);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            imgPost = itemView.findViewById(R.id.imgPostImage);
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
        }
    }
}