package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.EnchantAbility;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.resources.ResourcesClientEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EnchantMultishot extends EnchantAbility {

    public EnchantMultishot() {
        super(UnitAction.ENCHANT_MULTISHOT, ResourceCosts.ENCHANT_MULTISHOT);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Sacrifice",
                new ResourceLocation("minecraft", "textures/item/crossbow.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.SACRIFICE,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.SACRIFICE),
                null,
                List.of(
                        FormattedCharSequence.forward("Multishot Enchantment", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE005  " + EnchantAbility.RANGE, MyRenderer.iconStyle),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Enchant a Pillager's crossbow with Multishot, ", Style.EMPTY),
                        FormattedCharSequence.forward("making it fire 3 arrows at once in a spread.", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    protected boolean isCorrectUnitAndEquipment(LivingEntity entity) {
        return false;
    }

    @Override
    protected boolean hasEnchant(LivingEntity entity) {
        return false;
    }

    @Override
    protected void doEnchant(LivingEntity entity) { }
}
