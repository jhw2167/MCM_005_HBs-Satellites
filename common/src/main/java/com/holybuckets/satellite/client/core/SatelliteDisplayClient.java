package com.holybuckets.satellite.client.core;

import com.holybuckets.foundation.client.ClientEventRegistrar;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.client.CommonClassClient;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.blay09.mods.balm.api.event.client.BlockHighlightDrawEvent;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity.REACH_DIST_BLOCKS;

public class SatelliteDisplayClient extends SatelliteDisplay {

    private static LinkedHashSet<SatelliteDisplayClient> DISPLAYS = new LinkedHashSet<>();
    private final LinkedHashSet<BlockPos> holoBlocks;
    public SatelliteDisplayClient(Level level, SatelliteControllerBlockEntity controller) {
        super(level, controller);
        this.controller = controller;
        this.displayBlocks = new HashMap<>();
        this.holoBlocks = new LinkedHashSet<>();
        DISPLAYS.add(this);
    }


    public void add(BlockPos blockPos, ISatelliteDisplayBE displayBlock) {
        displayBlocks.put(blockPos, displayBlock);
        updateBounds(blockPos);
    }

    private void updateBounds(BlockPos pos) {
        minX = Math.min(minX, pos.getX());
        maxX = Math.max(maxX, pos.getX());
        minZ = Math.min(minZ, pos.getZ());
        maxZ = Math.max(maxZ, pos.getZ());
    }

    private void recalculateBounds() {
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minZ = Integer.MAX_VALUE;
        maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : displayBlocks.keySet()) {
            updateBounds(pos);
        }
    }

    public boolean isHitWithinDisplay(Vec3 hit) {
        double epsilon = 0.001;
        double adjustedMinX = minX - epsilon;
        double adjustedMaxX = maxX + 1.0 + epsilon;
        double adjustedMinZ = minZ - epsilon;
        double adjustedMaxZ = maxZ + 1.0 + epsilon;

        double adjustedMinY = controller.getBlockPos().getY() + 1.0 - epsilon;
        double adjustedMaxY = controller.getBlockPos().getY() + 4.0 + epsilon;

        return !(hit.x < adjustedMinX || hit.x > adjustedMaxX
            || hit.z < adjustedMinZ || hit.z > adjustedMaxZ
            || hit.y < adjustedMinY || hit.y > adjustedMaxY);
    }

    @Override
    public void addAll(Map<BlockPos, ISatelliteDisplayBE> blocks) {
        super.addAll(blocks);
        for(BlockPos pos : blocks.keySet()) {
            for(int i=1; i<=controller.getHeight(); i++) {
                holoBlocks.add(pos.above(i));
            }
        }
        DISPLAYS.add(this);
    }


    public void clear() {
        displayBlocks.clear();
        holoBlocks.clear();
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minZ = Integer.MAX_VALUE;
        maxZ = Integer.MIN_VALUE;
        DISPLAYS.remove(this);
    }


    //** Statics


    public static void init(ClientEventRegistrar reg) {
        reg.registerOnBlockHighlightDraw(SatelliteDisplayClient::onBlockHighlightDraw);
    }

    private static void onBlockHighlightDraw(BlockHighlightDrawEvent event)
    {
        Camera camera = event.getCamera();
        if (!(camera.getEntity() instanceof Player player)) {
            return;
        }

        BlockHitResult hitResult = (BlockHitResult) player.pick(REACH_DIST_BLOCKS, 0.5f, true);
        Level level = camera.getEntity().level();
        if (hitResult.getType() != HitResult.Type.BLOCK) return;
        for(SatelliteDisplayClient disp : DISPLAYS) {
            if(disp.holoBlocks.contains((hitResult.getBlockPos()))) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
