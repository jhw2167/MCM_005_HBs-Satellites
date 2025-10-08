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
        BlockHitResult target = event.getTarget();
        Level level = event.getCamera().getEntity().level();
        if(SatelliteMain.chiselBitsApi.isViewingHoloBlock(level, target)) {
            event.setCanceled(true);
        }
    }


}
