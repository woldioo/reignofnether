package com.solegendary.reignofnether.votesystem;
import com.google.gson.Gson;

import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.solegendary.reignofnether.ReignOfNether;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;

import java.util.ArrayList;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.JsonElement;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.Map;


import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod.EventBusSubscriber(modid = ReignOfNether.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT, Dist.DEDICATED_SERVER})
public class MapDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Path CONFIG_MAPS_DIRECTORY = Paths.get("config/mapvote");
    private static final MapDataLoader INSTANCE = new MapDataLoader();

    private final List<MapData> maps = new ArrayList<>();

    public MapDataLoader() {
        super(GSON, CONFIG_MAPS_DIRECTORY.toString());
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        maps.clear();
        System.out.println("Starting to load maps from config/reignofnether/maps...");

        // Load JSON files from the config directory
        if (Files.exists(CONFIG_MAPS_DIRECTORY) && Files.isDirectory(CONFIG_MAPS_DIRECTORY)) {
            try {
                Files.list(CONFIG_MAPS_DIRECTORY)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            System.out.println("Attempting to load resource: " + path);

                            try (FileReader reader = new FileReader(path.toFile())) {
                                // Load map data
                                MapData mapData = GSON.fromJson(reader, MapData.class);
                                maps.add(mapData);
                                System.out.println("Successfully loaded map: " + mapData.getName());

                                // Debugging gamerules parsing
                                Map<String, Object> gamerules = mapData.getGamerules();
                                gamerules.forEach((key, value) -> {
                                    if (value instanceof Boolean) {
                                        System.out.println("Gamerule " + key + " is a boolean: " + value);
                                    } else if (value instanceof Number) {
                                        System.out.println("Gamerule " + key + " is an integer: " + ((Number) value).intValue());
                                    } else {
                                        System.out.println("Gamerule " + key + " is of an unknown type: " + value.getClass().getSimpleName());
                                    }
                                });

                            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                                System.err.println("Failed to parse JSON in resource: " + path);
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                System.err.println("Failed to read files from config directory: " + CONFIG_MAPS_DIRECTORY);
                e.printStackTrace();
            }
        } else {
            System.err.println("Maps directory does not exist: " + CONFIG_MAPS_DIRECTORY);
        }

        System.out.println("Finished loading maps. Total maps loaded: " + maps.size());
    }


    public static List<MapData> getLoadedMaps() {
        return INSTANCE.maps;
    }
}
