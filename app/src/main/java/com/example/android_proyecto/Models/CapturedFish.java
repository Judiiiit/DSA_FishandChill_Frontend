package com.example.android_proyecto.Models;

import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class CapturedFish {

    private String id;

    @SerializedName("fishSpecies")
    private Fish speciesFish;

    private double weight;
    private Timestamp captureTime;

    public CapturedFish() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Fish getSpeciesFish() { return speciesFish; }
    public void setSpeciesFish(Fish speciesFish) { this.speciesFish = speciesFish; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public Timestamp getCaptureTime() { return captureTime; }
    public void setCaptureTime(Timestamp captureTime) { this.captureTime = captureTime; }

    public String getSpeciesId() {
        return this.speciesFish != null ? this.speciesFish.getId() : null;
    }
}
