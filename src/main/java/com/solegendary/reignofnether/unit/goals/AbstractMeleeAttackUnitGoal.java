package com.solegendary.reignofnether.unit.goals;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.WitherSkeletonUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

// based on MeleeAttackGoal
public abstract class AbstractMeleeAttackUnitGoal extends Goal {
    protected final Mob mob;
    private final boolean followingTargetEvenIfNotSeen;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private final int tickPathRecalcMax = 5;
    private int ticksUntilNextAttack;
    private final int attackInterval;
    private long lastCanUseCheck;

    public AbstractMeleeAttackUnitGoal(Mob mob, int attackInterval, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.attackInterval = attackInterval;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void tickAttackCooldown() {
        if (ticksUntilNextAttack > 0) // tick down even when not targeting anything
            this.ticksUntilNextAttack -= 1;
    }

    public boolean canUse() {
        long i = this.mob.level.getGameTime();
        if (i - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = i;
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                }
            }
        }
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
            return false;
        } else {
            boolean canContinue = !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
            if (canContinue) {
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                this.mob.getNavigation().moveTo(this.path,  Unit.getSpeedModifier((Unit) this.mob));
            }
            return canContinue;
        }
    }

    public void start() {
        if (!((Unit) this.mob).getHoldPosition())
            this.mob.getNavigation().moveTo(this.path,  Unit.getSpeedModifier((Unit) this.mob));
        this.mob.setAggressive(true);
    }

    public void stop() {
        LivingEntity livingentity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.mob.setTarget(null);
        }
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double d0 = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (!((Unit) this.mob).getHoldPosition()) {
                if (ticksUntilNextPathRecalculation <= 0) {
                    Path path = mob.getNavigation().createPath(target.getX(), target.getY(), target.getZ(), 0);
                    this.mob.getNavigation().moveTo(path, Unit.getSpeedModifier((Unit) this.mob));
                    ticksUntilNextPathRecalculation = tickPathRecalcMax;
                } else {
                    ticksUntilNextPathRecalculation -= 1;
                }
            }
            this.checkAndPerformAttack(target, d0);
        }
    }

    protected void checkAndPerformAttack(LivingEntity target, double p_25558_) {
        double d0 = this.getAttackReachSqr(target);
        if (p_25558_ <= d0 && this.ticksUntilNextAttack <= 0) {
            this.ticksUntilNextAttack = this.adjustedTickDelay(attackInterval);
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
            //if (target instanceof WitherSkeletonUnit witherSkeletonUnit && witherSkeletonUnit.deathCloudTicks > 0)
            //    this.mob.addEffect(new MobEffectInstance(MobEffects.WITHER, (WitherSkeletonUnit.WITHER_SECONDS_ON_HIT * 20), 1));
        }
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected int getAttackInterval() {
        return this.adjustedTickDelay(attackInterval);
    }

    protected double getAttackReachSqr(LivingEntity p_25556_) {
        return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + p_25556_.getBbWidth());
    }
}
