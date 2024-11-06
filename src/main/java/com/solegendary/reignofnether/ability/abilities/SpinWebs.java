package com.solegendary.reignofnether.ability.abilities;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchSpiderWebs;
import com.solegendary.reignofnether.research.researchItems.ResearchWitherClouds;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpinWebs extends Ability {

    public static final int CD_MAX_SECONDS = 30;
    public static final int RANGE = 8;
    public static final int DURATION_SECONDS = 5;

    private final Spider spider;

    // bp and ticks to live
    private final ArrayList<WebBlock> webs = new ArrayList<>();

    private class WebBlock {
        public final BlockPos bp;
        public int tickAge = 0;
        public boolean isPlaced = false;

        public WebBlock(BlockPos bp) {
            this.bp = bp;
        }
    }

    public SpinWebs(Spider spider) {
        super(
            UnitAction.SPIN_WEBS,
            CD_MAX_SECONDS * ResourceCost.TICKS_PER_SECOND,
            RANGE,
            0,
            false,
            true
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
                () -> !ResearchClient.hasResearch(ResearchSpiderWebs.itemName),
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

            BlockPos originBp = MiscUtil.getHighestNonAirBlock(level, limitedBp, true);
            List<Vec2> vec2s = List.of(
                    new Vec2(0,0),
                    new Vec2(1,1),
                    new Vec2(-1,-1),
                    new Vec2(1,-1),
                    new Vec2(-1,1)
            );
            webs.clear();
            for (Vec2 vec2 : vec2s) {
                BlockPos bp = MiscUtil.getHighestNonAirBlock(level, limitedBp.offset(vec2.x, 0, vec2.y), true);
                if (((LivingEntity) unitUsing).distanceToSqr(Vec3.atCenterOf(bp)) < (RANGE * 2) * (RANGE * 2))
                    webs.add(new WebBlock(bp.above().above()));
            }

        } else if (level.isClientSide()) {
            if (((LivingEntity) unitUsing).isVehicle())
                HudClientEvents.showTemporaryMessage("Cannot use while mounted.");
        }
        this.setToMaxCooldown();
    }

    public void tick(Level level) {
        if (level.isClientSide() || webs.isEmpty())
            return;

        Collections.shuffle(webs);
        int i = 0;
        for (int j = 0; j < webs.size(); j++)
            if (!webs.get(j).isPlaced)
                i = j;

        if (!webs.get(i).isPlaced && level.getBlockState(webs.get(i).bp).getBlock() == Blocks.AIR) {
            BlockState bs = Blocks.COBWEB.defaultBlockState();
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, webs.get(i).bp, Block.getId(bs));
            level.levelEvent(bs.getSoundType().getPlaceSound().hashCode(), webs.get(i).bp, Block.getId(bs));
            level.setBlockAndUpdate(webs.get(i).bp, Blocks.COBWEB.defaultBlockState());
            webs.get(i).isPlaced = true;
        }
        else if (webs.get(i).isPlaced && webs.get(i).tickAge >= DURATION_SECONDS * 20 &&
            level.getBlockState(webs.get(i).bp).getBlock() == Blocks.COBWEB) {
            level.destroyBlock(webs.get(i).bp, false);
            webs.remove(i);
        }
        for (WebBlock web : webs)
            web.tickAge += 1;
    }
}
