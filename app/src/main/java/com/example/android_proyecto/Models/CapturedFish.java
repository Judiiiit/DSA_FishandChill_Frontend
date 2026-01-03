package com.example.android_proyecto.Models;

public class CapturedFish {
    private Fish fishSpecies;

    private double weight;

    private String captureTime;

    public CapturedFish() {}

    public Fish getFishSpecies() { return fishSpecies; }
    public void setFishSpecies(Fish fishSpecies) { this.fishSpecies = fishSpecies; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getCaptureTime() { return captureTime; }
    public void setCaptureTime(String captureTime) { this.captureTime = captureTime; }
}
