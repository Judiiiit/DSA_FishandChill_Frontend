package com.example.android_proyecto.Models;

import java.sql.Timestamp;

public class CapturedFish {

    private String id;          // uuid
    private Fish speciesFish;   // fish species information
    private double weight;
    private Timestamp captureTime; // time of capture

    public CapturedFish() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Fish getSpeciesFish() {
        return speciesFish;
    }

    public void setSpeciesFish(Fish speciesFish) {
        this.speciesFish = speciesFish;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Timestamp getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Timestamp captureTime) {
        this.captureTime = captureTime;
    }

    // Helper similar al del backend, por si te hace falta en la UI
    public String getSpeciesId() {
        return this.speciesFish != null ? this.speciesFish.getId() : null;
    }
}
