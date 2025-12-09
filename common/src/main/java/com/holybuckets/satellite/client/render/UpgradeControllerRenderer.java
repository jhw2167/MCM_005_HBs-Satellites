package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.client.CommonClassClient;
import com.holybuckets.satellite.block.be.UpgradeControllerBlockEntity;
import com.holybuckets.satellite.core.SatelliteManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class UpgradeControllerRenderer implements BlockEntityRenderer<UpgradeControllerBlockEntity> {

    public UpgradeControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(UpgradeControllerBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        poseStack.pushPose();

        // Get VertexConsumer AFTER pushPose and BEFORE building vertices
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Get wool texture from satellite controller
        ResourceLocation woolLoc = SatelliteManager.getResourceForColorId(blockEntity.getColorId());
        TextureAtlasSprite woolSprite = CommonClassClient.getSprite(woolLoc);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Use the wool sprite UVs
        float u0 = woolSprite.getU0();
        float v0 = woolSprite.getV0();
        float u1 = woolSprite.getU1();
        float v1 = woolSprite.getV1();

        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;

        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        // Same size and Y position as SatelliteControllerRenderer, but offset to the right
        float minX = 0.54f;  // 0.34f + 0.33f offset
        float maxX = 0.84f;  // 0.66f + 0.33f offset
        float minY = 0.1f;  // Same as SatelliteControllerRenderer
        float maxY = 0.22f;  // Same as SatelliteControllerRenderer
        float offset = 0.01f; // Small offset from face

        // Transform based on facing direction
        switch (facing) {
            case NORTH -> {
                // Face toward negative Z - Flip U coordinates
                builder.vertex(matrix, maxX, minY, -offset)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                builder.vertex(matrix, minX, minY, -offset)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                builder.vertex(matrix, minX, maxY, -offset)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                builder.vertex(matrix, maxX, maxY, -offset)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
            }
            case SOUTH -> {
                // Face toward positive Z
                builder.vertex(matrix, minX, minY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                builder.vertex(matrix, maxX, minY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                builder.vertex(matrix, maxX, maxY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                builder.vertex(matrix, minX, maxY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
            }
            case WEST -> {
                // Face toward negative X
                builder.vertex(matrix, -offset, minY, minX)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                builder.vertex(matrix, -offset, minY, maxX)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                builder.vertex(matrix, -offset, maxY, maxX)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                builder.vertex(matrix, -offset, maxY, minX)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
            }
            case EAST -> {
                // Face toward positive X
                builder.vertex(matrix, 1 + offset, minY, maxX)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                builder.vertex(matrix, 1 + offset, minY, minX)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                builder.vertex(matrix, 1 + offset, maxY, minX)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                builder.vertex(matrix, 1 + offset, maxY, maxX)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
            }
        }

        // Render upgrade textures in 4 quadrants above bottom row
        renderUpgradeTextures(blockEntity, poseStack, bufferSource, facing, matrix, normal, light, overlay);

        poseStack.popPose();
    }

    private void renderUpgradeTextures(UpgradeControllerBlockEntity blockEntity, PoseStack poseStack, 
                                     MultiBufferSource bufferSource, Direction facing, Matrix4f matrix, 
                                     Matrix3f normal, int light, int overlay) {
        
        var upgrades = blockEntity.getUpgrades();
        if (upgrades == null || upgrades.length == 0) return;

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Define quadrant positions above the bottom row (which is at y=0.05-0.20)
        // 0 1
        // 2 3
        //XZ start, XZ end, Y start, Y end
        float[][] quadrants = {
            {0.20f, 0.3f, 0.75f, 0.9f}, // Top-left quadrant
            {0.55f, 0.75f, 0.75f, 0.9f}, // Top-right quadrant
            {0.20f, 0.3f, 0.5f, 0.7f}, // Bottom-left quadrant
            {0.55f, 0.75f, 0.5f, 0.7f}  // Bottom-right quadrant
        };
        
        float offset = 0.01f;
        
        // Render up to 4 upgrades in the quadrants
        for (int i = 0; i < Math.min(4, upgrades.length); i++)
        {
            if (upgrades[i] == null) continue;
            
            // Get upgrade texture based on the dye color
            TextureAtlasSprite upgradeSprite = CommonClassClient.getSprite(upgrades[i].getUpgradeSpriteLocation());
            
            float u0 = upgradeSprite.getU0();
            float v0 = upgradeSprite.getV0();
            float u1 = upgradeSprite.getU1();
            float v1 = upgradeSprite.getV1();
            
            float[] quad = quadrants[i];
            float minX = quad[0];
            float maxX = quad[1];
            float minY = quad[2];
            float maxY = quad[3];
            
            // Transform based on facing direction
            switch (facing) {
                case NORTH -> {
                    builder.vertex(matrix, maxX, minY, -offset)
                        .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                    builder.vertex(matrix, minX, minY, -offset)
                        .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                    builder.vertex(matrix, minX, maxY, -offset)
                        .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                    builder.vertex(matrix, maxX, maxY, -offset)
                        .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                }
                case SOUTH -> {
                    builder.vertex(matrix, minX, minY, 1 + offset)
                        .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                    builder.vertex(matrix, maxX, minY, 1 + offset)
                        .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                    builder.vertex(matrix, maxX, maxY, 1 + offset)
                        .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                    builder.vertex(matrix, minX, maxY, 1 + offset)
                        .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                }
                case WEST -> {
                    builder.vertex(matrix, -offset, minY, minX)
                        .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                    builder.vertex(matrix, -offset, minY, maxX)
                        .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                    builder.vertex(matrix, -offset, maxY, maxX)
                        .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                    builder.vertex(matrix, -offset, maxY, minX)
                        .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                }
                case EAST -> {
                    builder.vertex(matrix, 1 + offset, minY, maxX)
                        .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                    builder.vertex(matrix, 1 + offset, minY, minX)
                        .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0,0).endVertex();
                    builder.vertex(matrix, 1 + offset, maxY, minX)
                        .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                    builder.vertex(matrix, 1 + offset, maxY, maxX)
                        .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                }
            }
        }
    }
}
