package com.example.android_proyecto.Models;

public class Fish {

    private String id;            // uuid
    private String speciesName;   // species name
    private int rarity;           // rarity level
    private double speciesWeight; // standard weight of the species

    public Fish() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public double getSpeciesWeight() {
        return speciesWeight;
    }

    public void setSpeciesWeight(double speciesWeight) {
        this.speciesWeight = speciesWeight;
    }
}
