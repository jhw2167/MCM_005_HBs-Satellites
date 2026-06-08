package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.client.CommonClassClient;
import com.holybuckets.satellite.core.SatelliteManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SatelliteRenderer implements BlockEntityRenderer<SatelliteBlockEntity> {

    public SatelliteRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SatelliteBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        int colorId = blockEntity.getColorId();
        if( colorId < 0 ) return;
        poseStack.pushPose();

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        ResourceLocation woolLoc = SatelliteManager.getResourceForColorId( colorId );
        TextureAtlasSprite woolSprite = CommonClassClient.getSprite(woolLoc);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float u0 = woolSprite.getU0();
        float v0 = woolSprite.getV0();
        float u1 = woolSprite.getU1();
        float v1 = woolSprite.getV1();

        // For a 6x6 square centered in 32x32:
        // Center at 13-18 pixels in both dimensions
        float minX = 13f/32f;  // 13/32 ≈ 0.406
        float maxX = 19f/32f;  // 19/32 ≈ 0.594
        float minY = 13f/32f;
        float maxY = 19f/32f;
        float offset = 0.01f;

        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;

        // North face - FIXED: Inverted vertex order
        PoseStack.Pose pose = poseStack.last();
        builder.addVertex(matrix, minX, maxY, -offset)
            .setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        builder.addVertex(matrix, maxX, maxY, -offset)
            .setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        builder.addVertex(matrix, maxX, minY, -offset)
            .setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        builder.addVertex(matrix, minX, minY, -offset)
            .setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);

// South face
        builder.addVertex(matrix, minX, minY, 1 + offset)
            .setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        builder.addVertex(matrix, maxX, minY, 1 + offset)
            .setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        builder.addVertex(matrix, maxX, maxY, 1 + offset)
            .setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        builder.addVertex(matrix, minX, maxY, 1 + offset)
            .setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);

// West face
        builder.addVertex(matrix, -offset, minY, minX)
            .setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);
        builder.addVertex(matrix, -offset, minY, maxX)
            .setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);
        builder.addVertex(matrix, -offset, maxY, maxX)
            .setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);
        builder.addVertex(matrix, -offset, maxY, minX)
            .setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);

// East face - FIXED: Inverted vertex order
        builder.addVertex(matrix, 1 + offset, maxY, minX)
            .setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
        builder.addVertex(matrix, 1 + offset, maxY, maxX)
            .setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
        builder.addVertex(matrix, 1 + offset, minY, maxX)
            .setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
        builder.addVertex(matrix, 1 + offset, minY, minX)
            .setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);


// Bottom face
        builder.addVertex(matrix, minX, -offset, minX)
            .setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        builder.addVertex(matrix, maxX, -offset, minX)
            .setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        builder.addVertex(matrix, maxX, -offset, maxX)
            .setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        builder.addVertex(matrix, minX, -offset, maxX)
            .setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        poseStack.popPose();
    }
}
