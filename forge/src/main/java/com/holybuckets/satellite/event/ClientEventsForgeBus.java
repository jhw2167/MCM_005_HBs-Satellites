package com.holybuckets.satellite.event;

import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = com.holybuckets.satellite.Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventsForgeBus {


    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Render after particles but before translucent blocks
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();

        // Your rendering code here
        SatelliteMain.chiselBitsApi.renderUiSphere(camera, poseStack);
    }

}
