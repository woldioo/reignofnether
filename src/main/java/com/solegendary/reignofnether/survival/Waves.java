package com.solegendary.reignofnether.survival;

import java.util.HashMap;
import java.util.Map;

public class Waves {

    public static int getNextPopulation(int waveNumber) {
        return WAVE_POPULATIONS.get(waveNumber);
    }

    private static final Map<Integer, Integer> WAVE_POPULATIONS = new HashMap<>();

    static {
        WAVE_POPULATIONS.put(1, 6);
        WAVE_POPULATIONS.put(2, 12);
        WAVE_POPULATIONS.put(3, 18);
        WAVE_POPULATIONS.put(4, 24);
        WAVE_POPULATIONS.put(5, 36);
        WAVE_POPULATIONS.put(6, 46);
        WAVE_POPULATIONS.put(7, 57);
        WAVE_POPULATIONS.put(8, 0);
        WAVE_POPULATIONS.put(9, 0);
        WAVE_POPULATIONS.put(10, 0);
        WAVE_POPULATIONS.put(11, 0);
        WAVE_POPULATIONS.put(12, 0);
        WAVE_POPULATIONS.put(13, 0);
        WAVE_POPULATIONS.put(14, 0);
        WAVE_POPULATIONS.put(15, 0);
        WAVE_POPULATIONS.put(16, 0);
        WAVE_POPULATIONS.put(17, 0);
        WAVE_POPULATIONS.put(18, 0);
        WAVE_POPULATIONS.put(19, 0);
        WAVE_POPULATIONS.put(20, 0);
    }
}
