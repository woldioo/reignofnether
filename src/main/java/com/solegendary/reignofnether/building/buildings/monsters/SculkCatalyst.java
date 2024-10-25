package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class SculkCatalyst extends Building implements NightSource {

    public final static String buildingName = "Sculk Catalyst";
    public final static String structureName = "sculk_catalyst";
    public final static ResourceCost cost = ResourceCosts.SCULK_CATALYST;

    public final static int nightRange = 25;
    public final static int nightRangeMax = 40;
    private final Set<BlockPos> nightBorderBps = new HashSet<>();

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
        this.buildTimeModifier = 2.0f;

        this.startingBlockTypes.add(Blocks.POLISHED_BLACKSTONE);
    }

    public int getNightRange() { return (isBuilt || isBuiltServerside) ? nightRange : 0; }

    public BlockPos getNightCentre() { return centrePos; }

    @Override
    public void onBuilt() {
        super.onBuilt();
        updateNightBorderBps();
    }

    @Override
    public void updateNightBorderBps() {
        this.nightBorderBps.clear();
        this.nightBorderBps.addAll(MiscUtil.getNightCircleBlocks(centrePos, getNightRange(), level, originPos));
    }

    @Override
    public Set<BlockPos> getNightBorderBps() {
        return nightBorderBps;
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    private static int numSculkBlocksNearby() {
        return 0;
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
                        FormattedCharSequence.forward("Distorts time to midnight within a " + nightRange + " block radius.", Style.EMPTY),
                        FormattedCharSequence.forward("Nearby sculk extends this range up to " + nightRangeMax + " and ", Style.EMPTY),
                        FormattedCharSequence.forward("provides absorption health.", Style.EMPTY)
                ),
                null
        );
    }
}
