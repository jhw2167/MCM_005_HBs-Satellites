package com.holybuckets.satellite.client.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.client.ClientEventRegistrar;
import com.holybuckets.foundation.event.custom.*;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.satellite.core.SatelliteWeaponsManager;
import com.holybuckets.satellite.particle.WoolDustHelper;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.blay09.mods.balm.api.event.client.ConnectedToServerEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SatelliteFlareWeapon {

    private static Map<BlockPos, HashMap<BlockPos, Waypoint>> activeWaypoints = new HashMap<>();
    public static int WAYPOINT_FLARE_MAX_DISTANCE = 512;
    public static int WAYPOINT_FLARE_MIN_DISTANCE = 4;      //delete waypoint when player gets close

    private static class Waypoint {
        String levelId;
        BlockPos targetPos;
        int colorId;
        boolean isActive;

        public static int activeCount = 0;

        public Waypoint(String levelId, BlockPos targetPos, int colorId) {
            this.levelId = levelId;
            this.targetPos = targetPos;
            this.colorId = colorId;
            setActive(CURRENT_LEVEL_ID, Minecraft.getInstance().player);
        }

        public void setActive(String currentLevelId, Player p)
        {
            this.isActive = false;
            if(!currentLevelId.equals(this.levelId) ) return;
            if(!HBUtil.BlockUtil.inRange(p.blockPosition(), this.targetPos, WAYPOINT_FLARE_MAX_DISTANCE)) return;
            this.isActive = true; activeCount++;
        }
    }

    public static String CURRENT_LEVEL_ID = "";
    public static void init(ClientEventRegistrar reg) {
        reg.registerOnConnectedToServer( SatelliteFlareWeapon::onConnectedToServer);
        reg.registerOnClientLevelTick(TickType.ON_120_TICKS, SatelliteFlareWeapon::onClient120Ticks);
        reg.registerOnSimpleMessage(SatelliteWeaponsManager.MSG_ID_WAYPOINT_FLARE, SatelliteFlareWeapon::setWayPointFlare);
        reg.registerOnRenderLevel(RenderLevelEvent.RenderStage.AFTER_PARTICLES, SatelliteFlareWeapon::tryRenderWaypointFlare);
    }

    private static void onConnectedToServer(ConnectedToServerEvent event) {
        activeWaypoints.clear();
        bufferBuilder = null;
    }

    private static void onClient120Ticks(ClientLevelTickEvent event)
    {
        CURRENT_LEVEL_ID = HBUtil.LevelUtil.toLevelId(Minecraft.getInstance().level);
        Waypoint.activeCount = 0;
        for (var map : activeWaypoints.values()) {
            Iterator<Map.Entry<BlockPos, Waypoint>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, Waypoint> entry = iterator.next();
                Waypoint w = entry.getValue();

                w.setActive(CURRENT_LEVEL_ID, Minecraft.getInstance().player);

                if (w.isActive) {
                    BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
                    BlockPos wpPos = w.targetPos.atY(playerPos.getY());

                    if (HBUtil.BlockUtil.inRange(playerPos, wpPos, WAYPOINT_FLARE_MIN_DISTANCE)) {
                        iterator.remove(); // Safe removal during iteration
                    }
                }
            }
        }
    }

    private static void setWayPointFlare(SimpleMessageEvent event)
    {
        JsonElement json = JsonParser.parseString( event.getContent() );
        if(json.isJsonNull() || !json.isJsonObject()) return;
        JsonObject obj = json.getAsJsonObject();

        if( !obj.has("satelliteControllerOrigin")
        || !obj.has("colorId")) return;

        BlockPos satControllerOrigin = HBUtil.BlockUtil.stringToBlockPos( obj.get("satelliteControllerOrigin").getAsString() );
        int colorId = obj.get("colorId").getAsInt();

        if(!obj.has("levelId")
        || !obj.has("targetControllerOrigin")
        || !obj.has("targetPos"))
        {   //Clear waypoint
            if(activeWaypoints.containsKey( satControllerOrigin ))
                activeWaypoints.get( satControllerOrigin ).clear();
            //Client message: Waypoints removed
            return;
        }

        Waypoint w = new Waypoint(
            obj.get("levelId").getAsString(),
            HBUtil.BlockUtil.stringToBlockPos( obj.get("targetPos").getAsString() ),
            colorId
        );

        BlockPos targetControllerOrigin = HBUtil.BlockUtil.stringToBlockPos( obj.get("targetControllerOrigin").getAsString() );
        activeWaypoints.putIfAbsent( satControllerOrigin, new HashMap<>() );
        Map<BlockPos, Waypoint> waypoints = activeWaypoints.get( satControllerOrigin );
        if( colorId == -1 ) {
            Waypoint wp = waypoints.remove( targetControllerOrigin );
            if(wp != null && wp.isActive) {
                //ClientMessager.bottomActionHint
            }

            return;
        }
        waypoints.put( targetControllerOrigin,  w );
    }

    private static BufferBuilder bufferBuilder = null;
    private static int MAX_BEACON_VERTICES = 256*1024; //256KB
    private static int MAX_CONCURRENT_BEACONS = 8; //Above 8 we render randomly by frame

    private static void tryRenderWaypointFlare(RenderLevelEvent event) {
        try {
            renderWaypointFlare(event);
        } catch (Exception ex) {
            //If we get an error rendering the beacon, likely due to buffer overflow, reset the buffer
            bufferBuilder = null;
            String msg = "SatelliteFlareWeapon: Error rendering waypoint flare visuals, resetting buffer. "+
                "This is not a critical error but let the author know if it happens repeatedly error:\n" + ex.getMessage();
            LoggerProject.logWarning("007000",  msg);
        }
    }

    private static void renderWaypointFlare(RenderLevelEvent event)
    {
        if(activeWaypoints.isEmpty()) return;

        if(bufferBuilder == null ) {
            bufferBuilder = new BufferBuilder(MAX_BEACON_VERTICES);
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        long gameTime = Minecraft.getInstance().level.getGameTime();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance()
            .renderBuffers().bufferSource();

        for (var waypoints : activeWaypoints.values()) {
            for (var wp : waypoints.values()) {

                if (!wp.isActive) continue;
                if(Waypoint.activeCount > MAX_CONCURRENT_BEACONS) {
                    //Randomly skip some waypoints to reduce overload
                    if( Math.random() > ((double)MAX_CONCURRENT_BEACONS / (double)Waypoint.activeCount) ) {
                        continue;
                    }
                }
                BlockPos targetPos = wp.targetPos;

                poseStack.pushPose();

                // CRITICAL: Translate relative to camera, not absolute world position
                poseStack.translate(
                    targetPos.getX() - cameraPos.x + 0.5,  // Center of block
                    targetPos.getY() - cameraPos.y,
                    targetPos.getZ() - cameraPos.z + 0.5   // Center of block
                );

                float[] colors = WoolDustHelper.getWoolColorRGB(wp.colorId);

                BeaconRenderer.renderBeaconBeam(
                    poseStack,
                    bufferSource,
                    BeaconRenderer.BEAM_LOCATION,
                    event.getPartialTick(),
                    1.0f,
                    gameTime,                              // FIXED: Use gameTime, not finishNanoTime
                    0,                                      // FIXED: Start at 0 (already translated)
                    Minecraft.getInstance().level.getMaxBuildHeight() - targetPos.getY(), // FIXED: Height to sky
                    colors,
                    0.2f,
                    0.25f
                );

                poseStack.popPose();
            }
        }

        //Must flush the buffer after rendering
        //bufferSource.endBatch();
    }


}
//END CLASS