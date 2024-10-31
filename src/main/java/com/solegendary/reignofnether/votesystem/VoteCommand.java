package com.solegendary.reignofnether.votesystem;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.DistExecutor;

import java.util.List;


public class VoteCommand {

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("vote")
                        .executes(context -> {
                            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VoteCommand::openMapSelectionScreen);
                            return 1;
                        })
        );
    }

    @OnlyIn(Dist.CLIENT)
    private static void openMapSelectionScreen() {
        Minecraft minecraftInstance = Minecraft.getInstance();
        List<MapData> maps = MapDataLoader.getLoadedMaps();  // Access loaded maps directly
        minecraftInstance.setScreen(new MapSelectionScreen(maps));
    }
}
