package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.registrars.EntityRegistrar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Random;

public class Wave {

    private static final Random random = new Random();

    int number;
    int population; // multiplied by number of players
    int highestUnitTier;

    public Wave(int number, int population, int highestUnitTier) {
        this.number = number;
        this.population = population;
        this.highestUnitTier = highestUnitTier;
    }

    public static Wave getWave(int number) {
        if (number <= 0)
            return Wave.getWave(0);
        if (number > WAVES.size())
            return Wave.getWave(WAVES.size() - 1);

        return WAVES.get(number - 1);
    }

    public static EntityType<? extends Mob> getRandomUnitOfTier(int tier) {
        return random.nextBoolean() ? TIER_1_UNITS.get(0) : TIER_1_UNITS.get(1);
    }

    private static final List<EntityType<? extends Mob>> TIER_1_UNITS = List.of(
            EntityRegistrar.ZOMBIE_UNIT.get(),
            EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get()
    );
    private static final List<EntityType<? extends Mob>> TIER_2_UNITS = List.of(
            EntityRegistrar.HUSK_UNIT.get(),
            EntityRegistrar.SKELETON_UNIT.get(),
            EntityRegistrar.SPIDER_UNIT.get()
    );
    private static final List<EntityType<? extends Mob>> TIER_3_UNITS = List.of(
            EntityRegistrar.DROWNED_UNIT.get(),
            EntityRegistrar.STRAY_UNIT.get(),
            EntityRegistrar.POISON_SPIDER_UNIT.get(),
            EntityRegistrar.CREEPER_UNIT.get()
            // + Spider Jockeys
    );
    private static final List<EntityType<? extends Mob>> TIER_4_UNITS = List.of(
            EntityRegistrar.ZOGLIN_UNIT.get(),
            EntityRegistrar.ENDERMAN_UNIT.get()
            // + Poison Spider Jockeys
    );
    private static final List<EntityType<? extends Mob>> TIER_5_UNITS = List.of(
            EntityRegistrar.WARDEN_UNIT.get()
            // + Charged creepers
    );

    private static final List<Wave> WAVES = List.of(
        new Wave(1, 5, 1),
        new Wave(2, 10, 1),
        new Wave(3, 15, 1),
        new Wave(4, 20, 2),
        new Wave(5, 25, 2),
        new Wave(6, 30, 2),
        new Wave(7, 35, 3),
        new Wave(8, 40, 3),
        new Wave(9, 45, 3),
        new Wave(10, 50, 4),
        new Wave(11, 55, 4),
        new Wave(12, 60, 4),
        new Wave(13, 65, 5),
        new Wave(14, 70, 5),
        new Wave(15, 75, 5)
    );
}
