package com.holybuckets.satellite.client.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.client.ClientEventRegistrar;
import com.holybuckets.foundation.event.custom.RenderLevelEvent;
import com.holybuckets.foundation.event.custom.SimpleMessageEvent;
import com.holybuckets.foundation.event.custom.TickType;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.satellite.particle.WoolDustHelper;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.balm.api.event.client.ConnectedToServerEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;

import java.util.HashMap;
import java.util.Map;

public class SatelliteWeapons {

    private static Map<BlockPos, Waypoint> activeWaypoints = new HashMap<>();

    public static int WAYPOINT_FLARE_MAX_DISTANCE = 512;
    private static class Waypoint {
        String levelId;
        BlockPos targetPos;
        int colorId;
        boolean isActive;

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
            this.isActive = true;
        }
    }

    public static String CURRENT_LEVEL_ID = "";
    public static void init(ClientEventRegistrar reg) {
        reg.registerOnConnectedToServer( SatelliteWeapons::onConnectedToServer);
        reg.registerOnClientLevelTick(TickType.ON_120_TICKS, e -> {
            CURRENT_LEVEL_ID = HBUtil.LevelUtil.toLevelId(Minecraft.getInstance().level);
            activeWaypoints.values().forEach(w-> w.setActive(CURRENT_LEVEL_ID, Minecraft.getInstance().player) );
        });
        reg.registerOnSimpleMessage(SatelliteManager.MSG_ID_WAYPOINT_FLARE, SatelliteWeapons::setWayPointFlare);
        reg.registerOnRenderLevel(RenderLevelEvent.RenderStage.AFTER_PARTICLES, SatelliteWeapons::renderWaypointFlare);
    }

    private static void onConnectedToServer(ConnectedToServerEvent event) {
        activeWaypoints.clear();
    }

    private static void setWayPointFlare(SimpleMessageEvent event)
    {
        JsonElement json = JsonParser.parseString( event.getContent() );
        if(json.isJsonNull() || !json.isJsonObject()) return;
        JsonObject obj = json.getAsJsonObject();
        if(!obj.has("levelId")
        || !obj.has("targetPos")
        || !obj.has("colorId")) return;

        Waypoint w = new Waypoint(
            obj.get("levelId").getAsString(),
            HBUtil.BlockUtil.stringToBlockPos( obj.get("targetPos").getAsString() ),
            obj.get("colorId").getAsInt()
        );

        if(activeWaypoints.containsKey( w.targetPos )) {
            activeWaypoints.remove( w.targetPos );
            return;
        }
        activeWaypoints.put( w.targetPos, w );
    }


    private static void renderWaypointFlare(RenderLevelEvent event)
    {
        if(activeWaypoints.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        long gameTime = Minecraft.getInstance().level.getGameTime();

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(
            new BufferBuilder(256) // Capacity for beacon vertices
        );

        for (Waypoint wp : activeWaypoints.values()) {
            if (!wp.isActive) continue;

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

        //Must flush the buffer after rendering
        bufferSource.endBatch();
    }


}
//END CLASS