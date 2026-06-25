package com.holybuckets.satellite.core;

import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import com.holybuckets.satellite.client.core.SatelliteFlareWeapon;
import net.minecraft.world.item.Items;

/**
 * Lifecycle entry point for satellite weapons. Owns the server-start hook and the
 * default-weapon registration; the actual fire/track/clear logic for each weapon
 * lives in its own class (e.g. {@link SatelliteFlareWeapon}).
 */
public class SatelliteWeaponManager {

    static void onBeforeServerStart() {
        addDefaultWeapons();
    }

    private static void addDefaultWeapons() {
        SatelliteFlareWeapon.SATELLITE_FLARE_DESIGNATOR_ITEM = Items.REDSTONE_TORCH;
        TargetControllerBlockEntity.addWeapon(SatelliteFlareWeapon.SATELLITE_FLARE_DESIGNATOR_ITEM, SatelliteFlareWeapon::fireWaypointMessage);
        TargetControllerBlockEntity.addWeaponClearHook(SatelliteFlareWeapon.SATELLITE_FLARE_DESIGNATOR_ITEM, SatelliteFlareWeapon::clearWaypoint);
        //TargetControllerBlockEntity.addWeapon(ModBlocks.satelliteDisplayBlock.asItem(), SatelliteFlareWeapon::fireWaypointMessage);
    }

}
//END CLASS
