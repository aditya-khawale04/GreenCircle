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
import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private Context context;
    private List<Plant> plantList;

    public PlantAdapter(Context context, List<Plant> plantList) {
        this.context = context;
        this.plantList = plantList;
    }

    // Method to update data when filtering
    public void updateList(List<Plant> newList) {
        this.plantList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        holder.tvName.setText(plant.getCommonName());
        holder.tvScientific.setText(plant.getScientificName());
        holder.tagSunlight.setText(plant.getSunlight());
        holder.tagSeason.setText(plant.getSeason());

        // Load Image
        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(plant.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgPlant);
        }
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlant;
        TextView tvName, tvScientific, tagSunlight, tagSeason;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlant = itemView.findViewById(R.id.imgPlant);
            tvName = itemView.findViewById(R.id.tvPlantName);
            tvScientific = itemView.findViewById(R.id.tvScientificName);
            tagSunlight = itemView.findViewById(R.id.tag1);
            tagSeason = itemView.findViewById(R.id.tag2);
        }
    }
}