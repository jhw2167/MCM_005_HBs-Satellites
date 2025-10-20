package com.holybuckets.satellite.client;

import com.holybuckets.foundation.event.ClientEventRegistrar;
import com.holybuckets.foundation.event.custom.BlockHighlightDrawEvent;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.HashSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SatelliteDisplayClient {
    private final SatelliteControllerBlockEntity controller;
    private final Map<BlockPos, ISatelliteDisplayBlock> displayBlocks;
    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minZ = Integer.MAX_VALUE;
    private int maxZ = Integer.MIN_VALUE;

    public SatelliteDisplayClient(SatelliteControllerBlockEntity controller) {
        this.controller = controller;
        this.displayBlocks = new HashMap<>();
    }

    public static void init(ClientEventRegistrar reg) {
        reg.registerOnBlockHighlightDraw(SatelliteDisplayClient::onBlockHighlightDraw);
    }

    private static void onBlockHighlightDraw(BlockHighlightDrawEvent event) {
        // Handle highlight drawing here
        // This will be called when blocks are highlighted in the client
    }

    public void propagateToNeighbors() {
        if (controller == null) return;

        Level level = controller.getLevel();
        if (level == null) return;

        // Find all connected display blocks via flood fill
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, ISatelliteDisplayBlock> nodes = new HashMap<>();
        Queue<BlockPos> toCheck = new LinkedList<>();

        // Start from controller position
        toCheck.offer(controller.getBlockPos());
        visited.add(controller.getBlockPos());

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            ISatelliteDisplayBlock temp = (ISatelliteDisplayBlock) level.getBlockEntity(current);
            nodes.put(current, temp);

            // Check all horizontal neighbors
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);

                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                BlockEntity be = level.getBlockEntity(neighbor);
                if (be instanceof ISatelliteDisplayBlock) {
                    toCheck.offer(neighbor);
                }
            }
        }

        displayBlocks.clear();
        displayBlocks.putAll(nodes);
        recalculateBounds();
    }

    public void add(BlockPos blockPos, ISatelliteDisplayBlock displayBlock) {
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

    public void clear() {
        displayBlocks.clear();
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minZ = Integer.MAX_VALUE;
        maxZ = Integer.MIN_VALUE;
    }
}
