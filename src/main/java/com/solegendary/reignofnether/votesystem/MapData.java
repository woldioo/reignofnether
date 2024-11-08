package com.solegendary.reignofnether.votesystem;

import java.util.Map;

import java.util.Map;

import java.util.Map;

public class MapData {
    private String name;
    private int players;
    private String description;
    private Map<String, Object> gamerules; // Change to Map<String, Object>
    private String image;

    public MapData(String name, int players, String description, Map<String, Object> gamerules, String image) {
        this.name = name;
        this.players = players;
        this.description = description;
        this.gamerules = gamerules;
        this.image = image;
    }

    // Getters
    public String getName() { return name; }
    public int getPlayers() { return players; }
    public String getDescription() { return description; }
    public Map<String, Object> getGamerules() { return gamerules; }
    public String getImage() { return image; }
}

