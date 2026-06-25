package com.holybuckets.satellite.client.core;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.foundation.core.MovingWaypoint;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.holybuckets.foundation.HBUtil.PlayerUtil;

/**
 * Server-side waypoint weapon. Fires a {@link MovingWaypoint} flare for the firing player
 * and tracks fired waypoints keyed by satellite controller origin so they can be cleared
 * when the controller's state changes (color change, destruction, UI target cleared, etc).
 *
 * NOTE: File location ({@code client/core/}) is legacy. The logic here is purely server-side;
 * the package may be moved to {@code com.holybuckets.satellite.core} in a future refactor.
 */
public class SatelliteFlareWeapon {
    public static Item SATELLITE_FLARE_DESIGNATOR_ITEM = null;

    private static class Waypoint {
        final String playerId;
        final BlockPos targetPos;
        final int colorId;
        final BlockPos satelliteControllerOrigin;

        Waypoint(String playerId, BlockPos targetPos, int colorId, BlockPos satelliteControllerOrigin) {
            this.playerId = playerId;
            this.targetPos = targetPos;
            this.colorId = colorId;
            this.satelliteControllerOrigin = satelliteControllerOrigin;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Waypoint w)) return false;
            return colorId == w.colorId && Objects.equals(playerId, w.playerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerId, colorId);
        }
    }

    // Tracks fired waypoints keyed by satellite controller origin.
    private static final Map<BlockPos, Set<Waypoint>> waypoints = new HashMap<>();

    public static void fireWaypointMessage(TargetControllerBlockEntity controller, ItemStack stack) {
        if (controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;
        if (!(controller.getPlayerFiredWeapon() instanceof ServerPlayer player)) return;

        String playerId = PlayerUtil.getId(player);
        if (playerId == null) return;

        BlockPos targetPos = controller.getUiTargetBlockPos();
        if (targetPos == null) return;

        int colorId = controller.getTargetColorId();
        BlockPos origin = controller.getSatelliteController().getBlockPos();

        targetPos = targetPos.atY(controller.getLevel().getMinBuildHeight());
        MovingWaypoint.setWaypoint(player, targetPos, colorId);
        // Remember this waypoint so clear methods can find it later.
        waypoints.computeIfAbsent(origin, k -> new HashSet<>())
            .add(new Waypoint(playerId, targetPos, colorId, origin));

        Messager.getInstance().sendBottomActionHint(player,
            "Waypoint flare fired at " + HBUtil.BlockUtil.positionToString(targetPos));
    }

    // Clears every tracked waypoint linked to this satellite controller.
    public static void clearWaypoints(SatelliteControllerBlockEntity controller) {
        if (controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;

        Set<Waypoint> tracked = waypoints.remove(controller.getBlockPos());
        if (tracked == null) return;
        for (Waypoint w : tracked) {
            MovingWaypoint.removeWaypoint(w.playerId, w.colorId);
        }
    }

    /**
     * Clear-hook entry point. Matches the {@code BiConsumer<TargetControllerBlockEntity, ItemStack>}
     * shape used by {@link TargetControllerBlockEntity#addWeaponClearHook}; called when the player
     * requests the target controller to clear its current target.
     */
    public static void clearWaypoint(TargetControllerBlockEntity controller, ItemStack stack) {
        if (controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;
        if (!(controller.getPlayerFiredWeapon() instanceof ServerPlayer player)) return;

        SatelliteControllerBlockEntity sat = controller.getSatelliteController();
        if (sat == null) return;

        clearWaypoint(sat, player, controller.getTargetColorId());
    }

    // Clears one waypoint matched by (player, colorId) under the given satellite controller.
    public static void clearWaypoint(SatelliteControllerBlockEntity controller, ServerPlayer player, int colorId) {
        if (controller == null || player == null) return;
        Set<Waypoint> tracked = waypoints.get(controller.getBlockPos());
        if (tracked == null) return;

        String playerId = PlayerUtil.getId(player);
        if (playerId == null) return;

        Waypoint key = new Waypoint(playerId, BlockPos.ZERO, colorId, controller.getBlockPos());
        if (tracked.remove(key)) {
            MovingWaypoint.removeWaypoint(playerId, colorId);
        }
        if (tracked.isEmpty()) waypoints.remove(controller.getBlockPos());
    }

    // Send currently active waypoints in the world to newly joined players.
    public static void sendAllActiveWaypoints(ServerPlayer player) {

    }

}
//END CLASS
