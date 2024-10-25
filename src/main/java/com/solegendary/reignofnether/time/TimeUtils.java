package com.solegendary.reignofnether.time;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TimeUtils {

    public static boolean isInRangeOfNightSource(Vec3 pos, boolean clientSide) {
        return isInRangeOfNightSource(pos, clientSide, null);
    }

    public static boolean isInRangeOfNightSource(Vec3 pos, boolean clientSide, BlockPos excludedOriginPos) {
        List<Building> buildings = clientSide ? BuildingClientEvents.getBuildings() : BuildingServerEvents.getBuildings();
        for (Building building : buildings) {
            if (building.isDestroyedServerside || building.originPos.equals(excludedOriginPos))
                continue;
            if (building instanceof NightSource ns) {
                BlockPos centrePos = BuildingUtils.getCentrePos(building.getBlocks());
                Vec2 centrePos2d = new Vec2(centrePos.getX(), centrePos.getZ());
                Vec2 pos2d = new Vec2((float) pos.x, (float) pos.z);
                return centrePos2d.distanceToSqr(pos2d) < Math.pow(ns.getNightRange(), 2);
            }
        }
        return false;
    }

    // use instead of level.isDay() as its much stricter for undead burning checks
    public static boolean isDay(Level level) {
        long time = level.getDayTime();
        return time >= 500 && time < 12500;
    }

    // more consistent version of Mob.isSunburnTick()
    public static boolean isSunBurnTick(Mob mob) {
        if (isDay(mob.level) && !mob.level.isClientSide) {
            BlockPos blockpos = new BlockPos(mob.getX(), mob.getEyeY(), mob.getZ());
            boolean flag = mob.isInWaterRainOrBubble() || mob.isInPowderSnow || mob.wasInPowderSnow;
            return !flag && mob.level.canSeeSky(blockpos);
        }
        return false;
    }
}
