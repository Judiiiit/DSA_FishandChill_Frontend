package com.example.android_proyecto.Models;

import com.google.gson.annotations.SerializedName;

public class SellCapturedFish {

    @SerializedName("fishSpeciesName")
    private String fishSpeciesName;

    @SerializedName("weight")
    private double weight;

    @SerializedName("captureTime")
    private String captureTime;

    @SerializedName("price")
    private int price;

    public SellCapturedFish(String fishSpeciesName, double weight, String captureTime, int price) {
        this.fishSpeciesName = fishSpeciesName;
        this.weight = weight;
        this.captureTime = captureTime;
        this.price = price;
    }

    public String getFishSpeciesName() { return fishSpeciesName; }
    public double getWeight() { return weight; }
    public String getCaptureTime() { return captureTime; }
    public int getPrice() { return price; }
}
