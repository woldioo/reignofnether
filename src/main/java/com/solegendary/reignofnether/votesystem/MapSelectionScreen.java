package com.solegendary.reignofnether.votesystem;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MapSelectionScreen extends Screen {

    private final List<MapData> maps;
    private final Map<MapData, Integer> votes = new HashMap<>();
    private long endTime;
    private boolean votingComplete = false;
    private MapData playerVote = null; // Track the player's current vote

    public MapSelectionScreen(List<MapData> maps) {
        super(Component.literal("Map Selection"));
        this.maps = maps;
        this.endTime = System.currentTimeMillis() + 20000; // 20-second timer

        // Initialize each map's votes to 0
        for (MapData map : maps) {
            votes.put(map, 0);
        }
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null) {
            System.err.println("Minecraft instance is not initialized.");
        }
    }
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Check if adding the next word exceeds the maximum width
            if (this.font.width(currentLine + word) <= maxWidth) {
                // Add the word to the current line
                currentLine.append(word).append(" ");
            } else {
                // Add the current line to the list and start a new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                }
                currentLine = new StringBuilder(word + " ");
            }
        }

        // Add any remaining text in the current line
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (votingComplete) return false;

        int thumbnailWidth = 100;
        int padding = 20;
        int startX = (this.width - ((thumbnailWidth + padding) * maps.size() - padding)) / 2;
        int startY = this.height / 4;

        for (int i = 0; i < maps.size(); i++) {
            MapData map = maps.get(i);
            int x = startX + i * (thumbnailWidth + padding);
            int y = startY;

            if (mouseX >= x && mouseX <= x + thumbnailWidth && mouseY >= y && mouseY <= y + 100) {
                if (playerVote != null) {
                    votes.put(playerVote, votes.get(playerVote) - 1); // Remove vote from previous map
                }

                playerVote = map; // Update to the new map choice
                votes.put(map, votes.get(map) + 1); // Add vote to the new map

                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(poseStack);
        } else {
            System.err.println("Minecraft instance is null during rendering.");
            return;
        }

        int thumbnailWidth = 100;
        int padding = 30;
        int startX = (this.width - ((thumbnailWidth + padding) * maps.size() - padding)) / 2;
        int startY = this.height / 4;

        // Check if the voting period has ended
        if (System.currentTimeMillis() >= endTime && !votingComplete) {
            votingComplete = true;
            selectWinningMap();
        }

        // Render timer countdown
        long timeLeft = Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
        this.font.draw(poseStack, "Time Left: " + timeLeft + "s", this.width / 2 - 50, startY - 30, 0xFFFFFF);

        for (int i = 0; i < maps.size(); i++) {
            MapData map = maps.get(i);
            int x = startX + i * (thumbnailWidth + padding);
            int y = startY;

            // Load and render map thumbnail
            Path configPath = new File("config/mapvote/" + map.getImage()).toPath();
            int thumbnailHeight = 0;
            if (Files.exists(configPath)) {
                try {
                    NativeImage nativeImage = NativeImage.read(Files.newInputStream(configPath));
                    DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                    ResourceLocation dynamicLoc = this.minecraft.getTextureManager().register("map_" + i, dynamicTexture);
                    RenderSystem.setShaderTexture(0, dynamicLoc);
                    thumbnailHeight = 100;
                    blit(poseStack, x, y, 0, 0, thumbnailWidth, thumbnailHeight, thumbnailWidth, thumbnailHeight);
                } catch (IOException e) {
                    System.err.println("Failed to load image for map: " + map.getName() + " from path: " + configPath);
                    e.printStackTrace();
                }
            } else {
                System.err.println("Image file does not exist for map: " + map.getName() + " at path: " + configPath);
            }

            // Render map information
            int infoStartY = y + thumbnailHeight + 5;
            this.font.draw(poseStack, map.getName(), x, infoStartY, 0xFFFFFF);

            List<String> wrappedDescription = wrapText(map.getDescription(), 150); // Set maxWidth to your desired width

            int lineHeight = 10; // Set the height of each line
            int descriptionStartY = infoStartY + 10;

            for (String line : wrappedDescription) {
                this.font.draw(poseStack, line, x, descriptionStartY, 0xCCCCCC);
                descriptionStartY += lineHeight; // Move down for the next line
            }

            this.font.draw(poseStack, "Players: " + map.getPlayers(), x, descriptionStartY, 0xAAAAAA);


            // Render vote count and percentage if voting is still ongoing
            if (!votingComplete) {
                int voteCount = votes.get(map);
                int totalVotes = votes.values().stream().mapToInt(Integer::intValue).sum();
                float percentage = totalVotes > 0 ? (voteCount * 100.0f / totalVotes) : 0;
                this.font.draw(poseStack, "Votes: " + voteCount + " (" + String.format("%.1f", percentage) + "%)", x, infoStartY + 40, 0xFFFF00);
            }
        }

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void selectWinningMap() {
        MapData winningMap = maps.stream().max(Comparator.comparingInt(votes::get)).orElse(null);
        if (winningMap != null) {
            System.out.println("The winning map is: " + winningMap.getName());
            // Here you can add logic to proceed with the selected map
        }
    }
}
