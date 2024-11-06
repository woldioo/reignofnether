package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.EnchantAbility;
import com.solegendary.reignofnether.ability.EnchantAbilityServerboundPacket;
import com.solegendary.reignofnether.building.buildings.villagers.Library;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.units.villagers.PillagerUnit;
import com.solegendary.reignofnether.unit.units.villagers.VindicatorUnit;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.HashMap;
import java.util.List;

public class EnchantSharpness extends EnchantAbility {

    private static final UnitAction ENCHANT_ACTION = UnitAction.ENCHANT_SHARPNESS;
    private static final Enchantment actualEnchantment = Enchantments.SHARPNESS;

    public EnchantSharpness(Library library) {
        super(ENCHANT_ACTION, library, ResourceCosts.ENCHANT_SHARPNESS);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Sharpness Enchantment",
                new ResourceLocation("minecraft", "textures/item/iron_axe.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == ENCHANT_ACTION || library.autoCastEnchant == this,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(ENCHANT_ACTION),
                () -> {
                    EnchantAbilityServerboundPacket.setAutocastEnchant(ENCHANT_ACTION, library.originPos);
                    if (library.autoCastEnchant == this)
                        library.autoCastEnchant = null;
                    else
                        library.autoCastEnchant = this;
                },
                List.of(
                        FormattedCharSequence.forward("Sharpness Enchantment", Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Enchant a Vindicator's axe with Sharpness I, ", Style.EMPTY),
                        FormattedCharSequence.forward("raising its damage by 1.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Removes all other enchantments.", Style.EMPTY),
                        FormattedCharSequence.forward("Right click to auto-cast", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public boolean isCorrectUnitAndEquipment(LivingEntity entity) {
        return entity instanceof VindicatorUnit &&
                entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof AxeItem;
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
        ItemStack item = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        EnchantmentHelper.setEnchantments(new HashMap<>(), item);
        item.enchant(actualEnchantment, 1);
    }
}
