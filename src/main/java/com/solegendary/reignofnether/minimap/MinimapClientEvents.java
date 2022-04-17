package com.solegendary.reignofnether.minimap;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MinimapClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    public static final int MAP_RADIUS = 100; // how many world blocks should be mapped
    public static final int RENDER_RADIUS = 50; // actual size on GUI
    private static final int REFRESH_TICKS_MAX = 100;
    private static int REFRESH_TICKS_CURRENT = 100;
    private static final float CORNER_OFFSET = 10;

    private static final DynamicTexture MAP_TEXTURE = new DynamicTexture(MAP_RADIUS*2, MAP_RADIUS*2, true);
    private static final RenderType MAP_RENDER_TYPE = RenderType.textSeeThrough(Minecraft.getInstance()
            .textureManager.register(ReignOfNether.MOD_ID + "_" + "minimap", MAP_TEXTURE));

    private static List<Integer> mapColours = new ArrayList<>();
    private static int centreX = (int) MC.player.getX();
    private static int centreZ = (int) MC.player.getZ();

    private static final Set<Block> BLOCK_IGNORE_LIST = Set.of(
            Blocks.FERN,
            Blocks.GRASS,
            Blocks.TALL_GRASS,
            Blocks.WHEAT,
            Blocks.MELON_STEM,
            Blocks.POTATOES,
            Blocks.CARROTS,
            Blocks.BEETROOTS,
            Blocks.BROWN_MUSHROOM,
            Blocks.RED_MUSHROOM
    );

    public static void setMapCentre(double x, double z) {
        centreX = (int) x;
        centreZ = (int) z;
    }

    public static void updateMapTexture()
    {
        NativeImage pixels = MAP_TEXTURE.getPixels();
        if (pixels != null)
        {
            int i = 0;
            for (int y = 0; y < MAP_RADIUS*2; y ++) {
                for (int x = 0; x < MAP_RADIUS*2; x ++) {
                    pixels.setPixelRGBA(x, y, mapColours.get(i));
                    i += 1;
                }
            }
            MAP_TEXTURE.upload();
        }
    }

    private static void updateMapColours()
    {
        if (MC.level == null || MC.player == null)
            return;

        long timeBefore = System.currentTimeMillis();

        mapColours = new ArrayList<>();
        for (int z = centreZ - MAP_RADIUS; z < centreZ + MAP_RADIUS; z++)
        {
            for (int x = centreX - MAP_RADIUS; x < centreX + MAP_RADIUS; x++) {

                int y = MC.level.getChunkAt(new BlockPos(x,0,z)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                Block block;
                do {
                    block = MC.level.getBlockState(new BlockPos(x,y,z)).getBlock();
                    if (BLOCK_IGNORE_LIST.contains(block))
                        y -= 1;
                    else
                        break;
                } while (true);

                int yNorth = MC.level.getChunkAt(new BlockPos(x,0,z-1)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z-1);
                Block blockNorth;
                do {
                    blockNorth = MC.level.getBlockState(new BlockPos(x,yNorth,z-1)).getBlock();
                    if (BLOCK_IGNORE_LIST.contains(blockNorth))
                        yNorth -= 1;
                    else
                        break;
                } while (true);

                Material mat = MC.level.getBlockState(new BlockPos(x,yNorth,z-1)).getMaterial();
                int col = mat.getColor().col;

                // shade blocks to give elevation effects, excluding liquids and nonblocking blocks (eg. grass, flowers)
                if (!mat.isLiquid()) {
                    if (yNorth > y)
                        col = shadeRGB(col, 0.82F);
                    else if (yNorth < y) {
                        col = shadeRGB(col, 1.16F);
                    }
                }
                else { // shade liquid based on depth
                    int depth = 0;
                    int depthMax = 20;
                    Material matBelow;
                    do {
                        depth += 1;
                        matBelow = MC.level.getBlockState(new BlockPos(x,y-depth,z)).getMaterial();
                    }
                    while(matBelow.isLiquid() && depth < depthMax);

                    // only reduce shade every nth step to have the map look sharper
                    depth = (int) (5*(Math.ceil(Math.abs(depth/5))));

                    col = shadeRGB(col, 1.2F - (0.025F * depth));
                }

                // append 0xFF to include 100% alpha (<< 4 shifts by 1 hex digit)
                mapColours.add(reverseRGB(col) | (0xFF << 24));
            }
        }
        System.out.println("updated in: " + (System.currentTimeMillis() - timeBefore) + "ms");
        System.out.println("blocks: " + mapColours.size());
    }

    // lightens or darkens a hex RGB value
    private static int shadeRGB(int col, float mult) {
        int red = (col >> 16) & 0xFF;
        int green = (col >> 8) & 0xFF;
        int blue = (col) & 0xFF;

        if (mult > 1) { // prevent colours going > 255 (0xFF)
            red = Math.min(Math.round(red * mult), 0xFF);
            green = Math.min(Math.round(green * mult), 0xFF);
            blue = Math.min(Math.round(blue * mult), 0xFF);
        }
        else { // prevent colours going < 0
            red = Math.max(Math.round(red * mult), 0);
            green = Math.max(Math.round(green * mult), 0);
            blue = Math.max(Math.round(blue * mult), 0);
        }
        return (red << 16) | (green << 8) | (blue);
    }

    // convert col from RGB -> BGR (for some reason setPixelRGBA reads them backwards)
    private static int reverseRGB(int col) {
        int red = (col >> 16) & 0xFF;
        int green = (col >> 8) & 0xFF;
        int blue = (col) & 0xFF;

        return (blue << 16) | (green << 8) | (red);
    }

    private static void renderMap(PoseStack stack)
    {
        Matrix4f matrix4f = stack.last().pose();

        // place vertices in a diamond shape - left, centre, right, top, centre, bottom
        float xl = MC.getWindow().getGuiScaledWidth() - (RENDER_RADIUS * 2) - CORNER_OFFSET;
        float xc = MC.getWindow().getGuiScaledWidth() - RENDER_RADIUS - CORNER_OFFSET;
        float xr = MC.getWindow().getGuiScaledWidth() - CORNER_OFFSET;
        float yt = MC.getWindow().getGuiScaledHeight() - (RENDER_RADIUS * 2) - CORNER_OFFSET;
        float yc = MC.getWindow().getGuiScaledHeight() - RENDER_RADIUS - CORNER_OFFSET;
        float yb = MC.getWindow().getGuiScaledHeight() - CORNER_OFFSET;

        // background vertex coords need to be slightly larger
        float bg_offset = 6;
        float xl_bg = xl - bg_offset;
        float xc_bg = xc;
        float xr_bg = xr + bg_offset;
        float yt_bg = yt - bg_offset;
        float yc_bg = yc;
        float yb_bg = yb + bg_offset;

        // render map background first
        ResourceLocation iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/map_background.png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        // code taken from GuiComponent.blit()
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, xc_bg, yb_bg, 0.0F).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, xr_bg, yc_bg, 0.0F).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, xc_bg, yt_bg, 0.0F).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, xl_bg, yc_bg, 0.0F).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);

        // render map itself
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer consumer = buffer.getBuffer(MAP_RENDER_TYPE);
        consumer.vertex(matrix4f, xc, yb, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(255).endVertex();
        consumer.vertex(matrix4f, xr, yc, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(255).endVertex();
        consumer.vertex(matrix4f, xc, yt, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(255).endVertex();
        consumer.vertex(matrix4f, xl, yc, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(255).endVertex();

        buffer.endBatch();
    }

    // the quad that denotes which part of the map is being viewed right now
    private static void renderViewQuad(PoseStack stack) {
        // TODO: get 4 corners from MiscUtil.screenPosToWorldPos, then somehow convert those to GUI coords

        // we always know the vertices of the map GUI from renderMap() and the world coords of the centre
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseDragEvent.Pre evt) {
        // TODO: when clicking on map move player there
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post evt) {
        if (!OrthoviewClientEvents.isEnabled())
            return;

        REFRESH_TICKS_CURRENT -= 1;
        if (REFRESH_TICKS_CURRENT <= 0) {
            REFRESH_TICKS_CURRENT = REFRESH_TICKS_MAX;
            updateMapColours();
            updateMapTexture();
        }
        renderMap(evt.getMatrixStack());
        renderViewQuad(evt.getMatrixStack());
    }
}