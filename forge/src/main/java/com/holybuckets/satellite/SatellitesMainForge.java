package com.holybuckets.satellite;

import com.holybuckets.satellite.block.HoloBaseBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.holybuckets.satellite.api.ChiselBitsAPI;

@Mod( Constants.MOD_ID)
public class SatellitesMainForge {

    public SatellitesMainForge() {
        super();
        Balm.initialize(Constants.MOD_ID, CommonClass::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BalmClient.initialize(Constants.MOD_ID, CommonClass::initClient));
        
        // Register the block highlight event handler
        MinecraftForge.EVENT_BUS.addListener(SatellitesMainForge::onRenderBlockHighlight);
    }

    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        // Get the block being highlighted
        BlockHitResult hitResult = event.getTarget();
        BlockPos pos = hitResult.getBlockPos();
        Level level = event.getCamera().getEntity().level();
        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof ChiseledBlock)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof IMultiStateBlockEntity cbe)) return;
        Vec3 target = clamp(hitResult.getLocation(), pos, pos.getCenter());
        if(!(cbe.isInside(target))) return;
        BlockState internalState = cbe.getInAreaTarget(target)
            .get().getBlockInformation().getBlockState();
        if(internalState.getBlock() instanceof HoloBaseBlock) {
            event.setCanceled(true);
            ChiselBitsAPI.onRenderBlockHighlight(hitResult, pos, internalState);
        }
    }

        private static final double EPSILON = 0.0001;
    private static Vec3 clamp(Vec3 hitLoc, Vec3i pos, Vec3 center) {
        Vec3 target = hitLoc.subtract(pos.getX(), pos.getY(), pos.getZ());

        // Clamp to (EPSILON, 1.0 - EPSILON) range - handles all boundaries
        double x = Math.max(EPSILON, Math.min(1.0 - EPSILON, target.x));
        double y = Math.max(EPSILON, Math.min(1.0 - EPSILON, target.y));
        double z = Math.max(EPSILON, Math.min(1.0 - EPSILON, target.z));

        // Move point closer to center by EPSILON
        // If x > center.x, subtract EPSILON (move left toward center)
        // If x < center.x, add EPSILON (move right toward center)
        x += (center.x > x) ? EPSILON : -EPSILON;
        y += (center.y > y) ? EPSILON : -EPSILON;
        z += (center.z > z) ? EPSILON : -EPSILON;

        // Ensure we didn't push outside bounds after adjustment
        x = Math.max(EPSILON, Math.min(1.0 - EPSILON, x));
        y = Math.max(EPSILON, Math.min(1.0 - EPSILON, y));
        z = Math.max(EPSILON, Math.min(1.0 - EPSILON, z));

        return new Vec3(x, y, z);
    }

}
