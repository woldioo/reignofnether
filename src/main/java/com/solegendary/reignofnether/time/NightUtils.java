package com.solegendary.reignofnether.time;

import com.solegendary.reignofnether.building.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NightUtils {

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
                if (centrePos2d.distanceToSqr(pos2d) < Math.pow(ns.getNightRange(), 2))
                    return true;
            }
        }
        return false;
    }
}
