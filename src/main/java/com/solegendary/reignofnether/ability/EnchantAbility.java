package com.solegendary.reignofnether.ability;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesClientEvents;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class EnchantAbility extends Ability {

    public static final int CD_MAX = 1;
    public static final int RANGE = 10;
    public final ResourceCost cost;

    public EnchantAbility(UnitAction action, ResourceCost cost) {
        super(
                action,
                CD_MAX,
                RANGE,
                0,
                true,
                true
        );
        this.cost = cost;
    }

    public boolean canAfford(Building buildingUsing) {
        Resources res = null;
        if (buildingUsing.getLevel().isClientSide()) {
            res = ResourcesClientEvents.getOwnResources();
        } else {
            for (Resources resources : ResourcesServerEvents.resourcesList)
                if (resources.ownerName.equals(buildingUsing.ownerName))
                    res = resources;
        }
        if (res != null)
            return (res.food >= cost.food &&
                    res.wood >= cost.wood &&
                    res.ore >= cost.ore);
        return false;
    }

    protected boolean isCorrectUnitAndEquipment(LivingEntity entity) {
        return false;
    }

    protected boolean hasEnchant(LivingEntity entity) {
        return false;
    }

    protected void doEnchant(LivingEntity entity) { }

    @Override
    public void use(Level level, Building buildingUsing, LivingEntity targetEntity) {

        if (!level.isClientSide() &&
            targetEntity instanceof Unit unit &&
            unit.getOwnerName().equals(buildingUsing.ownerName) &&
            isCorrectUnitAndEquipment(targetEntity) &&
            !hasEnchant(targetEntity) &&
            canAfford(buildingUsing) &&
            targetEntity.distanceToSqr(Vec3.atCenterOf(buildingUsing.centrePos)) < RANGE * RANGE) {

            doEnchant(targetEntity);

        } else if (level.isClientSide()) {
            if (!(targetEntity instanceof Unit unit &&
                    unit.getOwnerName().equals(buildingUsing.ownerName))) {
                HudClientEvents.showTemporaryMessage("Can only enchant your own units");
            } else if (targetEntity.distanceToSqr(Vec3.atCenterOf(buildingUsing.centrePos)) >= RANGE * RANGE) {
                HudClientEvents.showTemporaryMessage("Unit is out of range");
            } else if (!isCorrectUnitAndEquipment(targetEntity)) {
                HudClientEvents.showTemporaryMessage("Can't enchant that type of unit");
            } else if (hasEnchant(targetEntity)) {
                HudClientEvents.showTemporaryMessage("Unit already has this enchantment");
            } else if (!canAfford(buildingUsing)) {
                HudClientEvents.showTemporaryMessage("Can't afford this enchantment");
            }
        }
    }
}
