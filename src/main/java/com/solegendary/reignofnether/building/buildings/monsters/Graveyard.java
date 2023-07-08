package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.unit.units.monsters.SkeletonUnitProd;
import com.solegendary.reignofnether.unit.units.monsters.ZombieUnitProd;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Graveyard extends ProductionBuilding {

    public final static String buildingName = "Graveyard";
    public final static String structureName = "graveyard";

    public Graveyard(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.MOSSY_STONE_BRICKS;
        this.icon = new ResourceLocation("minecraft", "textures/block/mossy_stone_bricks.png");

        this.foodCost = ResourceCosts.Graveyard.FOOD;
        this.woodCost = ResourceCosts.Graveyard.WOOD;
        this.oreCost = ResourceCosts.Graveyard.ORE;
        this.popSupply = ResourceCosts.Graveyard.SUPPLY;

        this.startingBlockTypes.add(Blocks.DEEPSLATE_BRICKS);

        this.explodeChance = 0.2f;

        if (level.isClientSide())
            this.productionButtons = Arrays.asList(
                ZombieUnitProd.getStartButton(this, Keybindings.keyQ),
                SkeletonUnitProd.getStartButton(this, Keybindings.keyW)
            );
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                Graveyard.buildingName,
                new ResourceLocation("minecraft", "textures/block/mossy_stone_bricks.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == Graveyard.class,
                () -> !BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) &&
                        !ResearchClient.hasCheat("modifythephasevariance"),
                () -> true,
                () -> BuildingClientEvents.setBuildingToPlace(Graveyard.class),
                null,
                List.of(
                        FormattedCharSequence.forward(Graveyard.buildingName, Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE001  " + ResourceCosts.Graveyard.WOOD, MyRenderer.iconStyle),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A field of the dead that can raise Zombies and Skeletons", Style.EMPTY)
                ),
                null
        );
    }
}
