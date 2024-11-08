package com.solegendary.reignofnether.research.researchItems;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.BuildingServerboundPacket;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.building.ProductionItem;
import com.solegendary.reignofnether.building.buildings.villagers.Library;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchGrandLibrary extends ProductionItem {

    public final static String itemName = "Grand Library";
    public final static ResourceCost cost = ResourceCosts.RESEARCH_GRAND_LIBRARY;

    public ResearchGrandLibrary(ProductionBuilding building) {
        super(building, cost.ticks);
        this.onComplete = (Level level) -> {
            if (this.building instanceof Library library)
                library.changeStructure(Library.upgradedStructureName);
        };
        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
    }

    public String getItemName() {
        return ResearchGrandLibrary.itemName;
    }

    public static Button getStartButton(ProductionBuilding prodBuilding, Keybinding hotkey) {
        return new Button(
                ResearchGrandLibrary.itemName,
                14,
                new ResourceLocation("minecraft", "textures/block/bookshelf.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                hotkey,
                () -> false,
                () -> ProductionItem.itemIsBeingProduced(ResearchGrandLibrary.itemName, prodBuilding.ownerName) ||
                        (prodBuilding instanceof Library library && library.isUpgraded()),
                () -> true,
                () -> BuildingServerboundPacket.startProduction(prodBuilding.originPos, itemName),
                null,
                List.of(
                        FormattedCharSequence.forward(ResearchGrandLibrary.itemName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        ResourceCosts.getFormattedTime(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Upgrades this library, unlocking more powerful enchantments.", Style.EMPTY)
                )
        );
    }

    public Button getCancelButton(ProductionBuilding prodBuilding, boolean first) {
        return new Button(
                ResearchGrandLibrary.itemName,
                14,
                new ResourceLocation("minecraft", "textures/block/bookshelf.png"),
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_bronze.png"),
                null,
                () -> false,
                () -> false,
                () -> true,
                () -> BuildingServerboundPacket.cancelProduction(prodBuilding.minCorner, itemName, first),
                null,
                null
        );
    }
}
