package com.holybuckets.satellite.event;

import com.holybuckets.satellite.client.CommonClassClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.holybuckets.satellite.Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT )
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
        CommonClassClient.renderUiSphere(camera, poseStack);
    }

}
