package com.solegendary.reignofnether.mixin;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.monsters.HauntedHouse;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Shadow public abstract ClientLevel.ClientLevelData getLevelData();

    @Shadow @Final private Minecraft minecraft;

    // plays sounds for orthoview players as though they were on the ground near their selected units/buildings
    @Inject(
            method = "playSound",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void playSound(double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSource,
                           float pVolume, float pPitch, boolean pDistanceDelay, long pSeed, CallbackInfo ci) {
        if (!OrthoviewClientEvents.isEnabled())
            return;

        ci.cancel();
        Vec3 soundPos = getOrthoviewSoundPos(new Vec3(pX, pY, pZ));

        double d0 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(soundPos.x(), soundPos.y(), soundPos.z());
        SimpleSoundInstance simplesoundinstance = new SimpleSoundInstance(
                pSoundEvent, pSource, pVolume, pPitch, RandomSource.create(pSeed), soundPos.x(), soundPos.y(), soundPos.z()
        );
        if (pDistanceDelay && d0 > 100.0) {
            double d1 = Math.sqrt(d0) / 40.0;
            this.minecraft.getSoundManager().playDelayed(simplesoundinstance, (int)(d1 * 20.0));
        } else {
            this.minecraft.getSoundManager().play(simplesoundinstance);
        }
    }

    // calculates the location of where local world sounds should play for an orthoview player based on the selected
    // units and/or buildings that are currently on the screen
    // eg. if the player has selected a unit at pos (4,6) but their player entity is at (34,52) and a sound plays at
    // (10,10), the new sound position should instead be (10,10) - (4,6) + (34,52) = (40,56)
    // if nothing is selected, then default to the centre of the screen
    private Vec3 getOrthoviewSoundPos(Vec3 originalPos) {
        Player player = this.minecraft.player;
        if (player == null)
            return originalPos;

        ArrayList<Vec3> posList = new ArrayList<>();

        for (LivingEntity entity : UnitClientEvents.getSelectedUnits())
            posList.add(entity.getEyePosition());
        for (Building building : BuildingClientEvents.getSelectedBuildings()) {
            BlockPos bp = BuildingUtils.getCentrePos(building.getBlocks());
            posList.add(new Vec3(bp.getX(), bp.getY(), bp.getZ()));
        }
        // remove any positions that aren't on the screen
        List<Vec3> posListOnScreen = posList.stream().filter(vec3 -> MinimapClientEvents.isXZinsideMap((int) vec3.x, (int) vec3.z)).toList();

        // calculate the average position
        Vec3 newPos = new Vec3(0,0,0);
        if (posListOnScreen.size() > 0) {
            for (Vec3 pos : posListOnScreen)
                newPos = newPos.add(pos);
            double m = 1D/posListOnScreen.size();
            newPos = newPos.multiply(m, m, m);
        } else {
            // do a similar kind of calculation to get the pos at the centre of the screen as in CursorClientEvents
            newPos = MiscUtil.getOrthoviewCentreWorldPos(this.minecraft);
        }
        // get the position for the sound as though the player was at position newPos
        Vec3 diffOriginalToNew = originalPos.add(newPos.multiply(-1,-1,-1));
        return new Vec3(player.getX(), player.getY(), player.getZ()).add(diffOriginalToNew);
    }

    @Shadow public void setGameTime(long pTime) { }
    @Shadow public void setDayTime(long pTime) { }

    private final long MIDNIGHT = 18000;
    private final long NOON = 6000;

    // when near a monster obelisk or mausoleum, speed up time towards midnight (in whichever direction is closest)
    @Inject(
            method = "tickTime",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void tickTime(CallbackInfo ci) {
        ci.cancel();

        Minecraft MC = Minecraft.getInstance();
        if (MC.player == null)
            return;
        Vec3 pos = MC.player.position();

        long timeDiff = 100L;
        long timeNow = this.getLevelData().getDayTime();
        long targetTime = UnitClientEvents.targetClientTime;
        long targetTimePlusHalfDay = targetTime + 12000;
        while (targetTimePlusHalfDay < 0)
            targetTimePlusHalfDay += 24000;
        while (targetTimePlusHalfDay >= 24000)
            targetTimePlusHalfDay -= 24000;

        if (targetTime < 12000 && (timeNow > targetTime && timeNow <= targetTimePlusHalfDay))
            timeDiff *= -1;
        else if (targetTime >= 12000 && (timeNow > targetTime || timeNow <= targetTimePlusHalfDay))
            timeDiff *= -1;

        if (Math.abs(timeNow - targetTime) < Math.abs(timeDiff)) {
            this.setGameTime(targetTime);
            this.setDayTime(targetTime);
            return;
        }
        long timeSet = this.getLevelData().getGameTime() + timeDiff;
        while (timeSet < 0)
            timeSet += 24000;
        while (timeSet >= 24000)
            timeSet -= 24000;
        this.setGameTime(timeSet);
        this.setDayTime(timeSet);
    }
}
