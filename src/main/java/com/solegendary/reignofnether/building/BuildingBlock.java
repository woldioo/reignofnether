package com.solegendary.reignofnether.building;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.List;

public class BuildingBlock {
    private BlockPos blockPos;
    private BlockState blockState; // ideal blockstate when placed, not actual world state

    private List<Material> materialsThatIgnoreState = List.of(Material.SCULK, Material.LEAVES, Material.GLASS);

    public BuildingBlock(BlockPos blockPos, BlockState blockState) {
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    public BlockPos getBlockPos() { return blockPos; }
    public BlockState getBlockState() { return blockState; }

    public void setBlockPos(BlockPos bp) { this.blockPos = bp; }
    public void setBlockState(BlockState bs) { this.blockState = bs; }

    // rotation should only ever be done on a relative BlockPos or it will rotate about world (0,0)
    public BuildingBlock rotate(LevelAccessor level, Rotation rotation) {
        return new BuildingBlock(
            this.blockPos.rotate(rotation),
            this.blockState.rotate(level, blockPos, rotation)
        );
    }

    public BuildingBlock move(LevelAccessor level, BlockPos offset) {
        return new BuildingBlock(
            this.blockPos.offset(offset),
            this.blockState
        );
    }

    public boolean isPlaced(Level level) {
        BlockState bs;
        if (level.isClientSide())
            bs = Minecraft.getInstance().level.getBlockState(this.blockPos);
        else
            bs = level.getBlockState(this.blockPos);

        // wall blockstates don't match unless the block above them is placed
        boolean isMatchingWallBlock = this.blockState.getBlock() instanceof WallBlock && bs.getBlock() == this.blockState.getBlock();

        // account for sculk sensors turning on and off constantly
        if (this.materialsThatIgnoreState.contains(this.blockState.getMaterial()) &&
            this.materialsThatIgnoreState.contains(bs.getMaterial()))
            return true;

        Block block1 = this.blockState.getBlock();
        Block block2 = bs.getBlock();
        if ((block1 instanceof StairBlock && block2 instanceof StairBlock) ||
            (block1 instanceof FenceBlock && block2 instanceof FenceBlock) ||
            (block1 instanceof WallBlock && block2 instanceof WallBlock) ||
            (block1 instanceof IronBarsBlock && block2 instanceof IronBarsBlock))
            return true;

        return !this.blockState.isAir() && (bs == this.blockState || isMatchingWallBlock);
    }
}