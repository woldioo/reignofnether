package com.solegendary.reignofnether.votesystem;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class MapSelectionScreen extends Screen {

    private final List<MapData> maps;

    public MapSelectionScreen(List<MapData> maps) {
        super(Component.literal("Map Selection"));
        this.maps = maps;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null) {
            System.err.println("Minecraft instance is not initialized.");
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.minecraft != null) {
            this.renderBackground(poseStack);
        } else {
            System.err.println("Minecraft instance is null during rendering.");
            return;
        }

        int thumbnailWidth = 64;
        int padding = 20;
        int startX = (this.width - ((thumbnailWidth + padding) * maps.size() - padding)) / 2;
        int startY = this.height / 4;

        for (int i = 0; i < maps.size(); i++) {
            MapData map = maps.get(i);
            int x = startX + i * (thumbnailWidth + padding);
            int y = startY;

            // Render Map Image
            ResourceLocation imageLoc = new ResourceLocation("minecraft", map.getImage());
            RenderSystem.setShaderTexture(0, imageLoc);
            // Height for each map thumbnail
            int thumbnailHeight = 64;
            blit(poseStack, x, y, 0, 0, thumbnailWidth, thumbnailHeight, thumbnailWidth, thumbnailHeight);

            // Render Map Information below the image
            int infoStartY = y + thumbnailHeight + 5;
            this.font.draw(poseStack, map.getName(), x, infoStartY, 0xFFFFFF); // Map name
            this.font.draw(poseStack, map.getDescription(), x, infoStartY + 10, 0xCCCCCC); // Map description
            this.font.draw(poseStack, "Players: " + map.getPlayers(), x, infoStartY + 20, 0xAAAAAA); // Player count
        }

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
