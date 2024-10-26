package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class SculkCatalyst extends Building implements NightSource {

    public final static String buildingName = "Sculk Catalyst";
    public final static String structureName = "sculk_catalyst";
    public final static ResourceCost cost = ResourceCosts.SCULK_CATALYST;

    private final static Random random = new Random();

    public final static int nightRangeMin = 25;
    public final static int nightRangeMax = 50;
    private final Set<BlockPos> nightBorderBps = new HashSet<>();

    private final static int SCULK_SEARCH_RANGE = 30;

    private final ArrayList<BlockPos> sculkBps = new ArrayList<>();

    public SculkCatalyst(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.SCULK_CATALYST;
        this.icon = new ResourceLocation("minecraft", "textures/block/sculk_catalyst_side.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 2.5f;

        this.startingBlockTypes.add(Blocks.POLISHED_BLACKSTONE);
    }

    public int getNightRange() {
        if (isBuilt || isBuiltServerside) {
            return Math.min(nightRangeMin + (sculkBps.size() / 4), nightRangeMax);
        }
        return 0;
    }

    public BlockPos getNightCentre() { return centrePos; }

    @Override
    public void onBuilt() {
        super.onBuilt();
        updateNightBorderBps();
    }

    @Override
    public void updateNightBorderBps() {
        updateSculkBps();
        this.nightBorderBps.clear();
        this.nightBorderBps.addAll(MiscUtil.getNightCircleBlocks(centrePos, getNightRange(), level, originPos));
    }

    @Override
    public Set<BlockPos> getNightBorderBps() {
        return nightBorderBps;
    }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (tickLevel.isClientSide && tickAge % 100 == 0 && TimeClientEvents.showNightRadius)
            updateNightBorderBps();
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    private void updateSculkBps() {
        sculkBps.clear();
        for (int x = centrePos.getX() - SCULK_SEARCH_RANGE / 2; x < centrePos.getX() + SCULK_SEARCH_RANGE / 2; x++) {
            for (int z = centrePos.getZ() - SCULK_SEARCH_RANGE / 2; z < centrePos.getZ() + SCULK_SEARCH_RANGE / 2; z++) {
                BlockPos topBp = new BlockPos(x, maxCorner.getY(), z);
                if (isPosInsideBuilding(topBp))
                    continue;

                int y = 0;
                BlockState bs;
                BlockPos bp;
                do {
                    y += 1;
                    bp = topBp.offset(0,-y,0);
                    bs = level.getBlockState(bp);
                } while (bs.isAir() && y < 10);

                if (bs.getBlock() == Blocks.SCULK || bs.getBlock() == Blocks.SCULK_VEIN)
                    sculkBps.add(bp);
            }
        }
        Collections.shuffle(sculkBps);
    }

    // returns the number of blocks converted
    private int restoreRandomSculk(int amount) {
        if (getLevel().isClientSide())
            return 0;
        int restoredSculk = 0;

        for (int i = 0; i < amount; i++) {
            BlockPos bp;
            BlockState bs;
            try {
                bp = sculkBps.get(i);
                bs = level.getBlockState(bp);
            } catch (IndexOutOfBoundsException e) {
                return restoredSculk;
            }
            if (bs.getBlock() == Blocks.SCULK) {
                for (BlockPos bpAdj : List.of(bp.below(), bp.north(), bp.south(), bp.east(), bp.west())) {
                    BlockState bsAdj = level.getBlockState(bpAdj);
                    if (!bsAdj.isAir()) {
                        level.setBlockAndUpdate(bp, bsAdj);
                        restoredSculk += 1;
                    }
                }
            }
            else if (bs.getBlock() == Blocks.SCULK_VEIN) {
                level.destroyBlock(bp, false);
                restoredSculk += 1;
            }
        }
        return restoredSculk;
    }

    public void destroyRandomBlocks(int amount) {
        if (getLevel().isClientSide() || amount <= 0)
            return;

        updateSculkBps();
        int restoredSculk = restoreRandomSculk(amount * 2);
        if (restoredSculk < amount)
            super.destroyRandomBlocks(amount - restoredSculk);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                SculkCatalyst.buildingName,
                new ResourceLocation("minecraft", "textures/block/sculk_catalyst_side.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == SculkCatalyst.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(SculkCatalyst.class),
                null,
                List.of(
                        FormattedCharSequence.forward(SculkCatalyst.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A pillar which spreads sculk when nearby units die.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Distorts time to midnight within a " + nightRangeMin + " block radius.", Style.EMPTY),
                        FormattedCharSequence.forward("Nearby sculk extends this range up to " + nightRangeMax + " and ", Style.EMPTY),
                        FormattedCharSequence.forward("provides absorption health.", Style.EMPTY)
                ),
                null
        );
    }
}
