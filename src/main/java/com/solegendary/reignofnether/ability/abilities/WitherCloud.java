package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchWitherClouds;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.WitherSkeletonUnit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class WitherCloud extends Ability {

    private static final int CD_MAX_SECONDS = 45;
    private static final int DURATION_SECONDS = 10;

    private final WitherSkeletonUnit witherSkeletonUnit;

    public WitherCloud(WitherSkeletonUnit witherSkeletonUnit) {
        super(
                UnitAction.WITHER_CLOUD,
                CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
                0,
                0,
                false,
                false
        );
        this.witherSkeletonUnit = witherSkeletonUnit;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Death Cloud",
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/wither_skeleton.png"),
                hotkey,
                () -> witherSkeletonUnit.deathCloudTicks > 0,
                () -> !ResearchClient.hasResearch(ResearchWitherClouds.itemName),
                () -> true,
                () -> UnitClientEvents.sendUnitCommand(UnitAction.WITHER_CLOUD),
                null,
                List.of(
                        FormattedCharSequence.forward("Death Cloud", Style.EMPTY),
                        FormattedCharSequence.forward("\uE004  " + CD_MAX_SECONDS + "s", MyRenderer.iconStyle),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Causes the Wither Skeleton to release a cloud", Style.EMPTY),
                        FormattedCharSequence.forward("of deadly wither around it for the next " + DURATION_SECONDS + " seconds.", Style.EMPTY),
                        FormattedCharSequence.forward("While active, any melee attackers become withered.", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {
        witherSkeletonUnit.deathCloudTicks = DURATION_SECONDS * ResourceCost.TICKS_PER_SECOND;
        this.setToMaxCooldown();
    }
}
