package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.EnchantAbility;
import com.solegendary.reignofnether.ability.EnchantAbilityServerboundPacket;
import com.solegendary.reignofnether.building.buildings.villagers.Library;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.units.villagers.EvokerUnit;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

public class EnchantVigor extends EnchantAbility {

    private static final UnitAction ENCHANT_ACTION = UnitAction.ENCHANT_VIGOR;
    public static final Enchantment actualEnchantment = Enchantments.UNBREAKING;
    public static final float cooldownMultiplier = 0.75f;

    public EnchantVigor(Library library) {
        super(ENCHANT_ACTION, library, ResourceCosts.ENCHANT_VIGOR);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Vigor Enchantment",
                new ResourceLocation("minecraft", "textures/item/iron_helmet.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == ENCHANT_ACTION || library.autoCastEnchant == this,
                () -> false,
                library::isUpgraded,
                () -> CursorClientEvents.setLeftClickAction(ENCHANT_ACTION),
                () -> {
                    EnchantAbilityServerboundPacket.setAutocastEnchant(ENCHANT_ACTION, library.originPos);
                    if (library.autoCastEnchant == this)
                        library.autoCastEnchant = null;
                    else
                        library.autoCastEnchant = this;
                },
                List.of(
                        FormattedCharSequence.forward("Vigor Enchantment", Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Gives an Evoker a wand enchanted with ", Style.EMPTY),
                        FormattedCharSequence.forward("Vigor, lowering their cooldowns by 25%", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Right click to auto-cast", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public boolean isCorrectUnitAndEquipment(LivingEntity entity) {
        return entity instanceof EvokerUnit && entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof BowItem;
    }

    @Override
    public boolean hasAnyEnchant(LivingEntity entity) {
        return !entity.getItemBySlot(EquipmentSlot.MAINHAND).getAllEnchantments().isEmpty();
    }

    @Override
    protected boolean hasSameEnchant(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.MAINHAND).getAllEnchantments().containsKey(actualEnchantment);
    }

    @Override
    protected void doEnchant(LivingEntity entity) {
        entity.getItemBySlot(EquipmentSlot.MAINHAND).enchant(actualEnchantment, 1);
    }
}
