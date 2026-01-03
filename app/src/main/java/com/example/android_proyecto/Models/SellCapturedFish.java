package com.example.android_proyecto.Models;

public class SellCapturedFish {
    private String fishSpeciesName;
    private String captureTime;
    private int price;

    public SellCapturedFish(String fishSpeciesName, String captureTime, int price) {
        this.fishSpeciesName = fishSpeciesName;
        this.captureTime = captureTime;
        this.price = price;
    }

    public String getFishSpeciesName() { return fishSpeciesName; }
    public void setFishSpeciesName(String fishSpeciesName) { this.fishSpeciesName = fishSpeciesName; }

    public String getCaptureTime() { return captureTime; }
    public void setCaptureTime(String captureTime) { this.captureTime = captureTime; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}
