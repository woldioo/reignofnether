package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.time.TimeUtils;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class SurvivalServerEvents {

    private static boolean isEnabled = true;
    private static int nextWaveNumber = 1;
    private static Faction nextFaction;
    private static int nextPopulation;
    private static Difficulty difficulty = Difficulty.EASY;
    private static final ArrayList<LivingEntity> enemies = new ArrayList<>();
    private static final int STARTING_EXTRA_SECONDS = 1200; // extra time for all difficulties on wave 1

    private static long lastTime = -1;

    // raise speed of day if
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent evt) {
        if (evt.level.isClientSide() || evt.phase != TickEvent.Phase.END)
            return;

        long time = evt.level.getDayTime();
        long normTime = TimeUtils.normaliseTime(evt.level.getDayTime());

        if (lastTime < 0) {
            lastTime = normTime;
            return;
        }

        if (lastTime <= TimeUtils.DAWN && normTime > TimeUtils.DAWN) {
            PlayerServerEvents.sendMessageToAllPlayers("Dawn breaks.");
            ((ServerLevel) evt.level).setDayTime(time + getDifficultyTimeModifier());
        }
        else if (lastTime <= TimeUtils.DUSK && normTime > TimeUtils.DUSK) {
            PlayerServerEvents.sendMessageToAllPlayers("Night falls...");
            startNextWave();
            ((ServerLevel) evt.level).setDayTime(time + getDifficultyTimeModifier());
        }

        lastTime = normTime;
    }

    // register here too for command blocks
    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent evt) {
        evt.getDispatcher().register(Commands.literal("debug-end-wave")
                .executes((command) -> {
                    for (LivingEntity entity : enemies)
                        entity.kill();
                    return 1;
                }));
        evt.getDispatcher().register(Commands.literal("debug-reset")
                .executes((command) -> {
                    resetWaves();
                    return 1;
                }));
        evt.getDispatcher().register(Commands.literal("rts-wave-survival").then(Commands.literal("enable")
                .executes((command) -> {
                    resetWaves();
                    return 1;
                })));
        evt.getDispatcher().register(Commands.literal("difficulty").then(Commands.literal("easy")
                .executes((command) -> {
                    setDifficulty(Difficulty.EASY);
                    return 1;
                })));
        evt.getDispatcher().register(Commands.literal("difficulty").then(Commands.literal("medium")
                .executes((command) -> {
                    setDifficulty(Difficulty.MEDIUM);
                    return 1;
                })));
        evt.getDispatcher().register(Commands.literal("difficulty").then(Commands.literal("hard")
                .executes((command) -> {
                    setDifficulty(Difficulty.HARD);
                    return 1;
                })));
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent evt) {
        if (evt.getEntity() instanceof Unit &&
            evt.getEntity() instanceof LivingEntity entity &&
            !evt.getLevel().isClientSide) {

            enemies.removeIf(e -> e.getId() == entity.getId());
            // TODO: sync with SurvivalClientEvents
        }
    }


    private enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        IMPOSSIBLE
    }

    // register here too for command blocks
    public static void setDifficulty(Difficulty diff) {
        difficulty = diff;
    }

    public static long getDifficultyTimeModifier() {
        return switch (difficulty) {
            default -> 0; // 10mins each day/night
            case MEDIUM -> 2400; // 8mins each day/night
            case HARD -> 4800; // 6mins each day/night
            case IMPOSSIBLE -> 7200; // 4mins each day/night
        };
    }

    public static boolean isEnabled() { return isEnabled; }

    public static void setEnabled(boolean enable) {
        isEnabled = enable;
        // TODO: set max population to 1000
    }

    public static List<LivingEntity> getCurrentEnemies() {
        return enemies;
    }

    public static boolean isWaveInProgress() {
        return !getCurrentEnemies().isEmpty();
    }

    public static void resetWaves() {
        for (LivingEntity entity : enemies)
            entity.kill();
        nextWaveNumber = 1;
        nextPopulation = Waves.getNextPopulation(nextWaveNumber);
    }

    // triggered at nightfall
    public static void startNextWave() {

    }

    // triggered when last enemy is killed
    public static void endCurrentWave() {
        // set time to morning + some time depending on difficulty
        // raise population according to some math + scale to number of players
        // increment waveNumber
    }

    public static List<LivingEntity> generateNewEnemies() {
        // types are based on current wave (don't allow just 1 of a big enemy early on)
        // capped by nextPopulation
        return null;
    }


    public static void spawnMonsterWave() {

    }

    public static void spawnIllagerWave() {

    }

    public static void spawnPiglinWave() {

    }
}
