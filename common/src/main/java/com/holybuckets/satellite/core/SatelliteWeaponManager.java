package com.holybuckets.satellite.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.foundation.core.MovingWaypoint;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SatelliteWeaponManager {

    private static class Waypoint {
        final ServerPlayer player;
        final BlockPos targetPos;
        final int colorId;
        final BlockPos satelliteControllerOrigin;

        Waypoint(ServerPlayer player, BlockPos targetPos, int colorId, BlockPos satelliteControllerOrigin) {
            this.player = player;
            this.targetPos = targetPos;
            this.colorId = colorId;
            this.satelliteControllerOrigin = satelliteControllerOrigin;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Waypoint w)) return false;
            return colorId == w.colorId && Objects.equals(player, w.player);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player, colorId);
        }
    }

    // Tracks fired waypoints keyed by satellite controller origin.
    private static final Map<BlockPos, Set<Waypoint>> waypoints = new HashMap<>();

    static void onBeforeServerStart() {
        addDefaultWeapons();
    }

    private static void addDefaultWeapons() {
        TargetControllerBlockEntity.addWeapon(Items.REDSTONE_TORCH, SatelliteWeaponManager::fireWaypointMessage);
        //TargetControllerBlockEntity.addWeapon(ModBlocks.satelliteDisplayBlock.asItem(), SatelliteWeaponManager::fireWaypointMessage );
    }

    public static void fireWaypointMessage(TargetControllerBlockEntity controller, ItemStack stack) {
        if (controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;
        if (!(controller.getPlayerFiredWeapon() instanceof ServerPlayer player)) return;

        BlockPos targetPos = controller.getUiTargetBlockPos();
        if (targetPos == null) return;

        int colorId = controller.getTargetColorId();
        BlockPos origin = controller.getSatelliteController().getBlockPos();

        MovingWaypoint.setWaypoint(player, targetPos, colorId);
        // Remember this waypoint so clear methods can find it later.
        waypoints.computeIfAbsent(origin, k -> new HashSet<>())
            .add(new Waypoint(player, targetPos, colorId, origin));

        Messager.getInstance().sendBottomActionHint(player,
            "Waypoint flare fired at " + HBUtil.BlockUtil.positionToString(targetPos));
    }

    // Clears every tracked waypoint linked to this satellite controller.
    public static void clearWaypoints(SatelliteControllerBlockEntity controller) {
        if (controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;

        Set<Waypoint> tracked = waypoints.remove(controller.getBlockPos());
        if (tracked == null) return;
        for (Waypoint w : tracked) {
            MovingWaypoint.removeWaypoint(w.player, w.colorId);
        }
    }

    // Clears one waypoint matched by (player, colorId) under the given satellite controller.
    public static void clearWaypoint(SatelliteControllerBlockEntity controller, ServerPlayer player, int colorId) {
        if (controller == null || player == null) return;
        Set<Waypoint> tracked = waypoints.get(controller.getBlockPos());
        if (tracked == null) return;

        Waypoint key = new Waypoint(player, BlockPos.ZERO, colorId, controller.getBlockPos());
        if (tracked.remove(key)) {
            MovingWaypoint.removeWaypoint(player, colorId);
        }
        if (tracked.isEmpty()) waypoints.remove(controller.getBlockPos());
    }

    // Send currently active waypoints in the world to newly joined players
    public static void sendAllActiveWaypoints(ServerPlayer player) {

    }
    //END METHOD

}
//END CLASS
