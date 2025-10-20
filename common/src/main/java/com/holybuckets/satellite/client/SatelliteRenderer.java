package com.holybuckets.satellite.client;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
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
        
        poseStack.pushPose();

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        ResourceLocation woolLoc = SatelliteManager.getResourceForColorId( blockEntity.getColorId() );
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

        // North face
        builder.vertex(matrix, minX, minY, -offset)
            .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, maxX, minY, -offset)
            .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, maxX, maxY, -offset)
            .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, minX, maxY, -offset)
            .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();

        // South face
        builder.vertex(matrix, minX, minY, 1 + offset)
            .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, maxX, minY, 1 + offset)
            .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, maxX, maxY, 1 + offset)
            .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, minX, maxY, 1 + offset)
            .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();

        // West face
        builder.vertex(matrix, -offset, minY, minX)
            .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, -offset, minY, maxX)
            .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, -offset, maxY, maxX)
            .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, -offset, maxY, minX)
            .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();

        // East face
        builder.vertex(matrix, 1 + offset, minY, minX)
            .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, 1 + offset, minY, maxX)
            .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, 1 + offset, maxY, maxX)
            .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, 1 + offset, maxY, minX)
            .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();

        // Top face
        builder.vertex(matrix, minX, 1 + offset, minY)
            .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, maxX, 1 + offset, minY)
            .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, maxX, 1 + offset, maxY)
            .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, minX, 1 + offset, maxY)
            .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();

        // Bottom face
        builder.vertex(matrix, minX, -offset, minY)
            .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, maxX, -offset, minY)
            .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, maxX, -offset, maxY)
            .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, minX, -offset, maxY)
            .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, -1, 0).endVertex();

        poseStack.popPose();
    }
}
