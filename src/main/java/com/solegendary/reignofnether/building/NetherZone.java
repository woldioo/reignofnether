package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.nether.NetherBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Random;

import java.util.HashSet;
import java.util.Set;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class NetherZone {

    private static final Logger LOGGER = Logger.getLogger(NetherZone.class.getName());

    private static final int MAX_TICKS = 5;
    private static final int MAX_CONVERTS_AFTER_CONSTANT_RANGE = 60;

    private final BlockPos origin;
    private final Random random = new Random();

    private boolean isRestoring = false;
    private final double maxRange;
    private double range;
    private int ticksLeft = MAX_TICKS;
    private int convertsAfterConstantRange = 0;

    public BlockPos getOrigin() { return origin; }
    public double getRange() { return range; }
    public double getMaxRange() { return maxRange; }
    public boolean isRestoring() { return isRestoring; }
    public int getTicksLeft() { return ticksLeft; }
    public int getConvertsAfterConstantRange() { return convertsAfterConstantRange; }

    public NetherZone(BlockPos origin, double maxRange, double range) {
        this.origin = origin;
        this.maxRange = maxRange;
        this.range = range;
    }

    private NetherZone(BlockPos origin, double maxRange, double range,
                       boolean isRestoring, int ticksLeft, int convertsAfterConstantRange) {
        this.origin = origin;
        this.maxRange = maxRange;
        this.range = range;
        this.isRestoring = isRestoring;
        this.ticksLeft = ticksLeft;
        this.convertsAfterConstantRange = convertsAfterConstantRange;
    }

    public static NetherZone getFromSave(BlockPos origin, double maxRange, double range,
                                         boolean isRestoring, int ticksLeft, int convertsAfterConstantRange) {
        return new NetherZone(origin, maxRange, range, isRestoring, ticksLeft, convertsAfterConstantRange);
    }

    public void startRestoring() {
        convertsAfterConstantRange = 0;
        isRestoring = true;
        LOGGER.info("Started restoring blocks.");
    }

    public boolean isDone() {
        boolean done = isRestoring && convertsAfterConstantRange >= MAX_CONVERTS_AFTER_CONSTANT_RANGE;
        if (done) {
            LOGGER.info("NetherZone restoration or conversion is complete.");
        }
        return done;
    }

    public void tick(ServerLevel level) {
        if (!level.isClientSide()) {
            if (--ticksLeft <= 0 && convertsAfterConstantRange < MAX_CONVERTS_AFTER_CONSTANT_RANGE) {
                if (!isRestoring) {
                    netherConvertTick(level);
                    if (range < maxRange) {
                        range += 0.1;
                    } else {
                        convertsAfterConstantRange++;
                    }
                } else {
                    overworldRestoreTick(level);
                    if (range > 0) {
                        range -= 0.05;
                    } else {
                        convertsAfterConstantRange++;
                    }
                }
                ticksLeft = MAX_TICKS;
            }
        }
    }

    private void overworldRestoreTick(ServerLevel level) {
        double restoreRange = range + 5;
        Set<BlockPos> bps = generateBlockPositions(restoreRange);

        int conversions = 0;
        for (BlockPos bp : bps) {
            if (bp.distSqr(origin) < range * range) continue;

            if (random.nextDouble() <= 0.10) {
                for (NetherZone ncz : BuildingServerEvents.netherZones) {
                    if (!ncz.isRestoring && bp.distSqr(ncz.origin) < ncz.maxRange * ncz.maxRange) {
                        continue;
                    }
                }

                BlockState bs = NetherBlocks.getOverworldBlock(level, bp);
                BlockState bsPlant = NetherBlocks.getOverworldPlantBlock(level, bp.above(), true);
                if (bs != null && !BuildingUtils.isPosPartOfAnyBuilding(level.isClientSide(), bp, true, (int) (maxRange * 2))) {
                    level.setBlockAndUpdate(bp, bs);
                    if (bsPlant != null) {
                        level.setBlockAndUpdate(bp.above(), bsPlant);
                    }
                    conversions++;
                }
            }
        }
        LOGGER.info("Overworld restore tick converted " + conversions + " blocks.");
    }

    private void netherConvertTick(ServerLevel level) {
        Set<BlockPos> bps = generateBlockPositions(range);
        double rangeSqr = range * range;
        double maxRangeSqr = maxRange * maxRange;

        int conversions = 0;
        for (BlockPos bp : bps) {
            double distSqr = bp.distSqr(origin);
            if (distSqr > rangeSqr) continue;

            double chance = (1 - (distSqr / maxRangeSqr)) / 10;
            if (isAdjacentToObsidianWater(level, bp)) chance = 1.0;

            if (random.nextDouble() <= chance) {
                BlockState bs = NetherBlocks.getNetherBlock(level, bp);
                BlockState bsPlant = NetherBlocks.getNetherPlantBlock(level, bp.above());
                if (bs != null && !BuildingUtils.isPosPartOfAnyBuilding(level.isClientSide(), bp, true, (int) (maxRange * 2))) {
                    level.setBlockAndUpdate(bp, bs);
                    if (bsPlant != null) {
                        level.setBlockAndUpdate(bp.above(), bsPlant);
                    }
                    conversions++;
                }
            }
        }
        LOGGER.info("Nether convert tick converted " + conversions + " blocks.");
    }

    private Set<BlockPos> generateBlockPositions(double range) {
        Set<BlockPos> bps = new HashSet<>();
        for (int x = (int) -range; x <= range; x++) {
            for (int y = (int) -range / 2; y <= range / 2; y++) {
                for (int z = (int) -range; z <= range; z++) {
                    bps.add(origin.offset(x, y, z));
                }
            }
        }
        return bps;
    }

    private boolean isAdjacentToObsidianWater(ServerLevel level, BlockPos bp) {
        if (level.getBlockState(bp).getBlock() == Blocks.WATER ||
                level.getBlockState(bp).getBlock() == Blocks.BUBBLE_COLUMN) {
            int adjObs = 0;
            if (level.getBlockState(bp.north()).getBlock() == Blocks.OBSIDIAN) adjObs++;
            if (level.getBlockState(bp.south()).getBlock() == Blocks.OBSIDIAN) adjObs++;
            if (level.getBlockState(bp.east()).getBlock() == Blocks.OBSIDIAN) adjObs++;
            if (level.getBlockState(bp.west()).getBlock() == Blocks.OBSIDIAN) adjObs++;
            return adjObs >= 3;
        }
        return false;
    }
}
