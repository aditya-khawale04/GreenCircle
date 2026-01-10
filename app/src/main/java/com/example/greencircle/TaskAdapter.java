package com.example.greencircle;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private OnTaskToggleListener listener;

    // Interface to pass the NEW state (true/false)
    public interface OnTaskToggleListener {
        void onTaskToggle(Task task, boolean isCompleted);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskToggleListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTaskType());
        holder.tvDate.setText("Due: " + task.getFormattedDueTime());

        // 1. Remove previous listener to prevent recycling issues
        holder.rbComplete.setOnClickListener(null);

        // 2. Set current state
        holder.rbComplete.setChecked(task.isCompleted());

        // 3. Handle Toggle Logic (Select / Deselect)
        holder.rbComplete.setOnClickListener(v -> {
            boolean newState = !task.isCompleted(); // Flip the state
            task.setCompleted(newState);            // Update local model
            holder.rbComplete.setChecked(newState); // Update UI

            if (listener != null) {
                listener.onTaskToggle(task, newState); // Notify Fragment
            }
        });

        // Styling (Blue/Purple/Red based on type)
        String type = task.getTaskType() != null ? task.getTaskType() : "";
        if (type.contains("Watering")) {
            holder.iconBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
            holder.imgIcon.setImageResource(R.drawable.water_drops);
            holder.imgIcon.setColorFilter(Color.parseColor("#3B82F6"));
        } else if (type.contains("Fertilizing")) {
            holder.iconBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FAF5FF")));
            holder.imgIcon.setImageResource(R.drawable.leaf);
            holder.imgIcon.setColorFilter(Color.parseColor("#A855F7"));
        } else {
            holder.iconBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
            holder.imgIcon.setImageResource(R.drawable.leaf);
            holder.imgIcon.setColorFilter(Color.parseColor("#EF4444"));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        RadioButton rbComplete;
        ImageView imgIcon;
        FrameLayout iconBg;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskName);
            tvDate = itemView.findViewById(R.id.tvDueDate);
            rbComplete = itemView.findViewById(R.id.rbComplete);
            imgIcon = itemView.findViewById(R.id.imgTaskIcon);
            iconBg = (FrameLayout) imgIcon.getParent();
        }
    }

}