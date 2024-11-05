package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.monsters.SculkCatalyst;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.monsters.EndermanUnit;
import com.solegendary.reignofnether.unit.units.monsters.SpiderUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyMath;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SpinWebs extends Ability {

    public static final int CD_MAX_SECONDS = 30;
    public static final int RANGE = 8;
    public static final int DURATION_SECONDS = 5;

    private final Spider spider;

    public SpinWebs(Spider spider) {
        super(
            UnitAction.SPIN_WEBS,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            RANGE,
            0,
            false
        );
        this.spider = spider;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey) {
        return new AbilityButton(
                "Spin Webs",
                new ResourceLocation("minecraft", "textures/block/cobweb.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.SPIN_WEBS,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.SPIN_WEBS),
                null,
                List.of(
                        FormattedCharSequence.forward("Spin Webs", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("\uE004  " + CD_MAX_SECONDS + "s  \uE005  " + RANGE, MyRenderer.iconStyle),
                        FormattedCharSequence.forward("Throw a bunch of sticky webs onto the  ", Style.EMPTY),
                        FormattedCharSequence.forward("field that decay after " + DURATION_SECONDS + " seconds.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Cannot be used while or recently mounted.", Style.EMPTY)
                ),
                this
        );
    }

    @Override
    public void use(Level level, Unit unitUsing, BlockPos targetBp) {

        if (!level.isClientSide()) {
            BlockPos limitedBp = MyMath.getXZRangeLimitedBlockPos(((LivingEntity) unitUsing).getOnPos(), targetBp, range);

            BlockPos originBp = MiscUtil.getHighestNonAirBlock(level, limitedBp);
            ArrayList<BlockPos> bpList = new ArrayList<>();
            List<Vec2> vec2s = List.of(
                    new Vec2(0,0),
                    new Vec2(1,1),
                    new Vec2(-1,-1),
                    new Vec2(1,-1),
                    new Vec2(-1,1)
            );
            if (level.getBlockState(originBp).getBlock() == Blocks.COBWEB) {
                vec2s = List.of(
                        new Vec2(0,1),
                        new Vec2(1,0),
                        new Vec2(0,-1),
                        new Vec2(-1,0)
                );
            }
            for (Vec2 vec2 : vec2s) {
                BlockPos bp = MiscUtil.getHighestNonAirBlock(level, limitedBp.offset(vec2.x, 0, vec2.y));
                if (((LivingEntity) unitUsing).distanceToSqr(Vec3.atCenterOf(bp)) < (RANGE * 2) * (RANGE * 2))
                    bpList.add(bp.above());
            }
            for (BlockPos bp : bpList) {
                if (level.getBlockState(bp).getBlock() == Blocks.AIR)
                    level.setBlockAndUpdate(bp, Blocks.COBWEB.defaultBlockState());
            }
            CompletableFuture.delayedExecutor(DURATION_SECONDS, TimeUnit.SECONDS).execute(() -> {
                for (BlockPos bp : bpList) {
                    if (level.getBlockState(bp).getBlock() == Blocks.COBWEB)
                        level.destroyBlock(bp, false);
                }
            });
        } else if (level.isClientSide()) {
            if (((LivingEntity) unitUsing).isVehicle())
                HudClientEvents.showTemporaryMessage("Cannot use while mounted.");
        }
        this.setToMaxCooldown();
    }
}
