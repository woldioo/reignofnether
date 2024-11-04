package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.util.Faction;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class WaveController {
    public int waveNumber;
    public Faction nextFaction;
    public int nextPopulation;

    public List<LivingEntity> getCurrentEnemies() {
        return List.of();
    }

    public boolean isWaveInProgress() {
        return !getCurrentEnemies().isEmpty();
    }

    // triggered at nightfall
    public void startNextWave() {

    }

    // triggered when last enemy is killed
    public void endCurrentWave() {
        // set time to morning + some time depending on difficulty
        // raise population according to some math + scale to number of players
        // increment waveNumber
    }

    public List<LivingEntity> generateNewEnemies() {
        // types are based on current wave (don't allow just 1 of a big enemy early on)
        // capped by nextPopulation
        return null;
    }

    public void spawnIllagerWave() {

    }

    public void spawnMonsterWave() {

    }

    public void spawnPiglinWave() {

    }
}
