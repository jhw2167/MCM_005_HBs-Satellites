package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.client.CommonClassClient;
import net.minecraft.client.gui.Font;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.core.SatelliteManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SatelliteControllerRenderer implements BlockEntityRenderer<SatelliteControllerBlockEntity> {

    private Font font;
    public SatelliteControllerRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @Override
    public void render(SatelliteControllerBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {

        poseStack.pushPose();

        // Get VertexConsumer AFTER pushPose and BEFORE building vertices
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Get texture
        ResourceLocation woolLoc = SatelliteManager.getResourceForColorId( blockEntity.getColorId() );
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

        //Render Text
        renderTargetPos(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

    }

    private void renderTargetPos(SatelliteControllerBlockEntity blockEntity, float partialTick,
                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                 int combinedLight, int combinedOverlay)
    {
        BlockPos targetPos = blockEntity.getUiPosition();
        if(targetPos == null) targetPos = blockEntity.getBlockPos();
        poseStack.pushPose();

        poseStack.translate(0.5, 0.925, 0.5);

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
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
        float scale = 0.01f;
        poseStack.scale(scale, scale, scale);

// Prepare text components
        String xText =  getTruncatedString("X:", targetPos.getX());
        String yText = getTruncatedString("Y:", targetPos.getY());
        String zText = getTruncatedString("Z:", targetPos.getZ());
        int textColor = 0x000000;

// Render X in left third
        poseStack.pushPose();

        poseStack.translate(-30, 0, 0);
        font.drawInBatch(xText, -font.width(xText) / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

// Render Y in middle third
        font.drawInBatch(yText, -font.width(yText) / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);

// Render Z in right third
        poseStack.pushPose();
        poseStack.translate(30, 0, 0);
        font.drawInBatch(zText, -font.width(zText) / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        poseStack.popPose();

    }

    private String getTruncatedString(String pre, int coord) {
        if(Math.abs(coord) < 999) {
            return pre + coord;
        }
        return Integer.toString(coord);
    }

}
