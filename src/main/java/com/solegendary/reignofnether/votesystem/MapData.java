package com.solegendary.reignofnether.votesystem;

import java.util.Map;

public class MapData {
    private String name;
    private String description;
    private int players;
    private String image;  // e.g., "yourmodid:textures/mapvote/valley_of_deceit.png"
    private Map<String, Boolean> gamerules; // New gamerules field

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPlayers() { return players; }
    public void setPlayers(int players) { this.players = players; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Map<String, Boolean> getGamerules() { return gamerules; }
    public void setGamerules(Map<String, Boolean> gamerules) { this.gamerules = gamerules; }
}
