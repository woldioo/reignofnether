package com.solegendary.reignofnether.ability;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.villagers.Library;
import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class EnchantAbilityServerboundPacket {

    UnitAction abilityAction;
    BlockPos buildingPos;

    public static void setAutocastEnchant(UnitAction ability, BlockPos buildingPos) {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player != null)
            PacketHandler.INSTANCE.sendToServer(new EnchantAbilityServerboundPacket(ability, buildingPos));
    }

    // packet-handler functions
    public EnchantAbilityServerboundPacket(UnitAction ability, BlockPos buildingPos) {
        this.abilityAction = ability;
        this.buildingPos = buildingPos;
    }

    public EnchantAbilityServerboundPacket(FriendlyByteBuf buffer) {
        this.abilityAction = buffer.readEnum(UnitAction.class);
        this.buildingPos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(abilityAction);
        buffer.writeBlockPos(buildingPos);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            Building building = BuildingUtils.findBuilding(false, buildingPos);
            if (building instanceof Library library) {
                Ability ability = null;
                for (Ability abl : library.getAbilities())
                    if (abl.action == abilityAction)
                        ability = abl;
                if (ability instanceof EnchantAbility enchantAbility) {
                    if (library.autoCastEnchant == enchantAbility)
                        library.autoCastEnchant = null;
                    else
                        library.autoCastEnchant = enchantAbility;
                }
            }
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}