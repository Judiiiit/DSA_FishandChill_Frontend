package com.example.android_proyecto.Models;

import java.util.List;
import java.util.Map;

public class Inventory {

    // Mismas estructuras que en el backend:
    // private Map<String, FishingRod> userRods;
    // private Map<String, List<CapturedFish>> capturedFishes;
    // private String equippedRodId;

    private Map<String, FishingRod> userRods;
    private Map<String, List<CapturedFish>> capturedFishes;
    private String equippedRodId; // nullable

    public Inventory() {
    }

    public Map<String, FishingRod> getUserRods() {
        return userRods;
    }

    public void setUserRods(Map<String, FishingRod> userRods) {
        this.userRods = userRods;
    }

    public Map<String, List<CapturedFish>> getCapturedFishes() {
        return capturedFishes;
    }

    public void setCapturedFishes(Map<String, List<CapturedFish>> capturedFishes) {
        this.capturedFishes = capturedFishes;
    }

    public String getEquippedRodId() {
        return equippedRodId;
    }

    public void setEquippedRodId(String equippedRodId) {
        this.equippedRodId = equippedRodId;
    }
}
