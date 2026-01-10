package com.example.greencircle;
public class Plant {
    private String plantId; // To store the document ID
    private String commonName;
    private String scientificName;
    private String sunlight;
    private String season;
    private String imageUrl;
    private int waterFrequencyDays;
    private int growthDurationDays;

    // Empty constructor needed for Firestore
    public Plant() {}

    public Plant(String commonName, String scientificName, String sunlight, String season, String imageUrl) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.sunlight = sunlight;
        this.season = season;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public String getCommonName() { return commonName; }
    public String getScientificName() { return scientificName; }
    public String getSunlight() { return sunlight; }
    public String getSeason() { return season; }
    public String getImageUrl() { return imageUrl; }

    public void setCommonName(String commonName) { this.commonName = commonName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public void setSunlight(String sunlight) { this.sunlight = sunlight; }
    public void setSeason(String season) { this.season = season; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setWaterFrequencyDays(int waterFrequencyDays) {
        this.waterFrequencyDays = waterFrequencyDays;
    }
    public void setGrowthDurationDays(int growthDurationDays) {
        this.growthDurationDays = growthDurationDays;
    }

    public int getGrowthDurationDays() {
        return growthDurationDays;
    }

    public int getWaterFrequencyDays() {
        return waterFrequencyDays;
    }
}