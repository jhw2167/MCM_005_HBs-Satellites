package com.holybuckets.satellite.client;

import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import com.holybuckets.satellite.core.SatelliteManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TargetControllerRenderer implements BlockEntityRenderer<TargetControllerBlockEntity> {

    private Font font;
    
    public TargetControllerRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @Override
    public void render(TargetControllerBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        poseStack.pushPose();

        // Get VertexConsumer AFTER pushPose and BEFORE building vertices
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Get texture
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

        float minX = 0.34f;
        float maxX = 0.66f;
        float minY = 0.05f;
        float maxY = 0.20f;
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

        poseStack.popPose();

        // Render coordinate information and buttons
        renderTargetInfo(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderTargetInfo(TargetControllerBlockEntity blockEntity, float partialTick,
                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                 int combinedLight, int combinedOverlay)
    {
        // Get the primary controller to retrieve satellite position
        SatelliteControllerBlockEntity primaryController = blockEntity.getSatelliteController();
        if (primaryController == null) {
            return; // Render no information if no primary controller
        }

        BlockPos targetPos = primaryController.getUiPosition();
        if (targetPos == null) {
            targetPos = BlockPos.ZERO;
        }

        poseStack.pushPose();

        poseStack.translate(0.5, 0.925, 0.5);

        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH:
                poseStack.translate(0, 0, -0.5);  // Move to north face
                poseStack.mulPose(Axis.YP.rotationDegrees(0));
                break;
            case SOUTH:
                poseStack.translate(0, 0, 0.5);   // Move to south face
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case WEST:
                poseStack.translate(-0.5, 0, 0);  // Move to west face
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case EAST:
                poseStack.translate(0.5, 0, 0);   // Move to east face
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                break;
        }

        poseStack.translate(0, 0, -0.01); // Tiny offset AFTER rotation to prevent z-fighting
        poseStack.mulPose(Axis.ZP.rotationDegrees(180)); // Flip text right-side up
        float scale = 0.008f; // Smaller scale to fit more content
        poseStack.scale(scale, scale, scale);

        int textColor = 0x000000;

        // Top section divided into 3 rows for coordinates
        float rowHeight = 15f;
        float startY = 35f;

        // X coordinate row
        poseStack.pushPose();
        poseStack.translate(0, startY, 0);
        String xLabel = "X:";
        String xValue = String.valueOf(targetPos.getX());
        font.drawInBatch(xLabel, -40, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        font.drawInBatch(xValue, 10, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        // Y coordinate row
        poseStack.pushPose();
        poseStack.translate(0, startY - rowHeight, 0);
        String yLabel = "Y:";
        String yValue = String.valueOf(targetPos.getY());
        font.drawInBatch(yLabel, -40, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        font.drawInBatch(yValue, 10, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        // Z coordinate row
        poseStack.pushPose();
        poseStack.translate(0, startY - rowHeight * 2, 0);
        String zLabel = "Z:";
        String zValue = String.valueOf(targetPos.getZ());
        font.drawInBatch(zLabel, -40, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        font.drawInBatch(zValue, 10, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        // Bottom section with two buttons (twice as tall as coordinate rows)
        float buttonY = startY - rowHeight * 3 - 10f; // Add some spacing
        
        // TARGET button (left)
        poseStack.pushPose();
        poseStack.translate(-25, buttonY, 0);
        String targetText = "TARGET";
        font.drawInBatch(targetText, -font.width(targetText) / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        // FIRE button (right)
        poseStack.pushPose();
        poseStack.translate(25, buttonY, 0);
        String fireText = "FIRE";
        font.drawInBatch(fireText, -font.width(fireText) / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        poseStack.popPose();
    }
}
