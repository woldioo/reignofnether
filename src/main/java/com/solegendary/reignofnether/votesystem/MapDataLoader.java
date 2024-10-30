package com.solegendary.reignofnether.votesystem;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;

public class MapDataLoader {
    private static final Gson GSON = new Gson();
    private static final String MAPS_DIRECTORY = "reignofnether/maps";

    public static List<MapData> loadMaps(ResourceManager resourceManager) {
        List<MapData> maps = new ArrayList<>();
        System.out.println("Querying path: " + MAPS_DIRECTORY);

        System.out.println("Starting to load maps from data/reignofnether/maps...");

        for (ResourceLocation resourceLocation : resourceManager.listResources(MAPS_DIRECTORY, path -> path.getPath().endsWith(".json")).keySet()) {
            System.out.println("Found resource: " + resourceLocation);
            System.out.println("Available resources: " + resourceManager.listResources(MAPS_DIRECTORY, path -> path.getPath().endsWith(".json")).keySet());

            try {
                // Open the resource as a stream
                Resource resource = resourceManager.getResource(resourceLocation).orElse(null);
                if (resource != null) {
                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        // Parse JSON data into MapData object
                        MapData mapData = GSON.fromJson(reader, MapData.class);
                        maps.add(mapData);
                        System.out.println("Successfully loaded map: " + mapData.getName());
                    }
                } else {
                    System.err.println("Resource " + resourceLocation + " could not be opened.");
                }
            } catch (JsonIOException | JsonSyntaxException e) {
                System.err.println("Failed to parse JSON in resource: " + resourceLocation);
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("I/O error while reading resource: " + resourceLocation);
                e.printStackTrace();
            }
        }

        System.out.println("Finished loading maps. Total maps loaded: " + maps.size());
        return maps;
    }
}
