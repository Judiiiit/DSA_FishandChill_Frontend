package com.example.android_proyecto.Utils;

public class FishSellCalculator {

    /**
     * rarity:
     * 1 = COMMON
     * 2 = RARE
     * 3 = LEGENDARY
     */
    public static int calculateCoins(double weight, int rarity) {
        double multiplier;

        switch (rarity) {
            case 2: // RARE
                multiplier = 1.5;
                break;
            case 3: // LEGENDARY
                multiplier = 2.0;
                break;
            case 1: // COMMON
            default:
                multiplier = 1.0;
                break;
        }

        int coins = (int) Math.round(weight * multiplier);
        return Math.max(coins, 1);
    }
}
