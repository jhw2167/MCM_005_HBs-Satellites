package com.holybuckets.satellite.core;

import com.google.gson.JsonObject;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.foundation.networking.SimpleStringMessage;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RedstoneTorchBlock;

public class SatelliteWeaponManager {

    static void onBeforeServerStart() {
        addDefaultWeapons();
    }

    private static void addDefaultWeapons() {
        TargetControllerBlockEntity.addWeapon(Items.REDSTONE_TORCH, SatelliteWeaponManager::fireWaypointMessage );
        //TargetControllerBlockEntity.addWeapon(ModBlocks.satelliteDisplayBlock.asItem(), SatelliteWeaponManager::fireWaypointMessage );
    }

    public static final String MSG_ID_WAYPOINT_FLARE = "satellite_waypoint_flare";
    public static void clearWaypoints(SatelliteControllerBlockEntity controller) {
        if(controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;
        JsonObject json = new JsonObject();
        json.addProperty("satelliteControllerOrigin", HBUtil.BlockUtil.positionToString(controller.getBlockPos()) );
        json.addProperty("colorId", -1);

        SimpleStringMessage.createAndFire(MSG_ID_WAYPOINT_FLARE, json.toString());
    }

    public static void fireWaypointMessage(TargetControllerBlockEntity controller, ItemStack stack) {
        fireWaypointMessage(controller, stack, false);
    }

    public static void fireWaypointMessage(TargetControllerBlockEntity controller, ItemStack stack, boolean clear)
     {
        if(controller == null || controller.getLevel() == null || controller.getLevel().isClientSide) return;
        BlockPos targetPos = controller.getUiTargetBlockPos();
        int color = (clear) ? -1 : controller.getTargetColorId();

        //use GSON to create json with valuies, convert to string and send simpleStringMessage to Client
        JsonObject json = new JsonObject();
        //String levelString = HBUtil.LevelUtil.toLevelId(HBUtil.LevelUtil.LevelNameSpace.CLIENT, controller.getLevel());
        BlockPos targetControllerOrigin = controller.getBlockPos();
        BlockPos satelliteControllerOrigin = controller.getSatelliteController().getBlockPos();
        String levelString = HBUtil.LevelUtil.toLevelId(controller.getLevel());

        json.addProperty("levelId", levelString.replace("SERVER", "CLIENT") );
        json.addProperty("satelliteControllerOrigin", HBUtil.BlockUtil.positionToString(satelliteControllerOrigin) );
        json.addProperty("targetControllerOrigin", HBUtil.BlockUtil.positionToString(targetControllerOrigin) );
        if(targetPos != null) {
            String target = HBUtil.BlockUtil.positionToString(targetPos);
            json.addProperty("targetPos", target);
            Messager.getInstance().sendBottomActionHint(controller.getPlayerFiredWeapon(),
                "Waypoint flare fired at " + target);
        }

        json.addProperty("colorId", color);

        SimpleStringMessage.createAndFire(MSG_ID_WAYPOINT_FLARE, json.toString());
    }

    // Send currently active waypoints in the world to newly joined players
    public static void sendAllActiveWaypoints(ServerPlayer player) {

    }
    //END METHOD

}
//END CLASS