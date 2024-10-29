package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.buildings.monsters.Laboratory;
import com.solegendary.reignofnether.building.buildings.monsters.SculkCatalyst;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Sacrifice extends Ability {

    private static final int CD_MAX = 0;
    private static final int RANGE = 8;

    public Sacrifice() {
        super(
                UnitAction.SACRIFICE,
                CD_MAX,
                RANGE,
                0,
                true
        );
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Sacrifice",
                new ResourceLocation("minecraft", "textures/item/iron_hoe.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.SACRIFICE,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.SACRIFICE),
                null,
                List.of(
                        FormattedCharSequence.forward("Sacrifice", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE005  " + RANGE, MyRenderer.iconStyle),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Kill a nearby friendly unit to spread sculk.", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Building buildingUsing, LivingEntity targetEntity) {

        if (!level.isClientSide() &&
            buildingUsing instanceof SculkCatalyst &&
            targetEntity instanceof Unit unit &&
            unit.getOwnerName().equals(buildingUsing.ownerName)) {

            if (targetEntity.distanceToSqr(Vec3.atCenterOf(buildingUsing.centrePos)) < RANGE * RANGE) {
                targetEntity.kill();
            }
        } else if (level.isClientSide()) {
            if (!(targetEntity instanceof Unit unit &&
                unit.getOwnerName().equals(buildingUsing.ownerName))) {
                HudClientEvents.showTemporaryMessage("Can only sacrifice your own units");
            } else if (targetEntity.distanceToSqr(Vec3.atCenterOf(buildingUsing.centrePos)) >= RANGE * RANGE) {
                HudClientEvents.showTemporaryMessage("Unit is out of range");
            }
        }
    }
}
