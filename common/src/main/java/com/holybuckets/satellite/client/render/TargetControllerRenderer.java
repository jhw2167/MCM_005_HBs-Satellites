package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import com.holybuckets.satellite.client.CommonClassClient;
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

        float minX = 0.48f;//0.34f;
        float maxX = 0.54f;// 0.66f;
        float minY = 0.16f;
        float maxY = 0.21f; // Cut off top half by reducing from 0.26f to 0.21f
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
        renderTargetInfo(blockEntity, partialTick, poseStack, bufferSource, light, packedOverlay);
    }

    static int textColor = 0x000000;

    // Font scale configuration
    private static final float COORD_LABEL_SCALE = 1.2f;
    private static final float COORD_VALUE_SCALE = 0.9f;


    // Y grows downward since we flipped Z earlier
    static float rowHeight = 23.5f;
    static float startY = 6.1f; // start higher on screen (negative moves up visually)
    static int labelMarginAdjust = -32;

    // Button positioning configuration - moved down 20%
    private static final float BUTTON_Y_OFFSET = (rowHeight * 3) + 10 + ((rowHeight * 3) + 10) * 0.2f;
    private static final float TARGET_BUTTON_X_OFFSET = -33f; // Moved slightly left from -28f
    private static final float FIRE_BUTTON_X_OFFSET = 36f; // Moved 20% right from 30f


    private void renderTargetInfo(TargetControllerBlockEntity blockEntity, float partialTick,
                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                 int combinedLight, int combinedOverlay)
    {
        // Get the primary controller to retrieve satellite position
        //SatelliteControllerBlockEntity mainController = blockEntity.getSatelliteController();
        BlockPos targetPos = BlockPos.ZERO;
        if (blockEntity.getUiTargetBlockPos() != null) {
            targetPos = blockEntity.getUiTargetBlockPos();
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
        float scale = 0.007f; // Decreased from 0.008f for slightly smaller font
        poseStack.scale(scale, scale, scale);

        float leftAlignOffset = -15;
// X coordinate (top)
        poseStack.pushPose();
        poseStack.translate(leftAlignOffset, startY, 0);
        drawCoordRow(poseStack, bufferSource, combinedLight, targetPos.getX(), "X");
        poseStack.popPose();

// Y coordinate (middle)
        poseStack.pushPose();
        poseStack.translate(leftAlignOffset, startY + rowHeight, 0);
        drawCoordRow(poseStack, bufferSource, combinedLight, targetPos.getY(), "Y");
        poseStack.popPose();

// Z coordinate (bottom)
        poseStack.pushPose();
        poseStack.translate(leftAlignOffset, startY + rowHeight * 2, 0);
        drawCoordRow(poseStack, bufferSource, combinedLight, targetPos.getZ(), "Z");
        poseStack.popPose();

// Buttons below coordinates
        float buttonY = startY + BUTTON_Y_OFFSET;
        poseStack.pushPose();
        poseStack.translate(TARGET_BUTTON_X_OFFSET, buttonY, 0);
        String targetOrClear = (blockEntity.getCursorPosition()==null) ? "TARGET" : "CLEAR";
        font.drawInBatch(targetOrClear, -font.width(targetOrClear) / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(FIRE_BUTTON_X_OFFSET, buttonY, 0);
        font.drawInBatch("FIRE", -font.width("FIRE") / 2f, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void drawCoordRow(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int value, String label) {
        int textColor = 0x000000;
        
        // Draw label with label scale
        poseStack.pushPose();
        poseStack.scale(COORD_LABEL_SCALE, COORD_LABEL_SCALE, COORD_LABEL_SCALE);
        font.drawInBatch(label + ":", labelMarginAdjust / COORD_LABEL_SCALE, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();
        
        // Draw value with value scale
        poseStack.pushPose();
        poseStack.scale(COORD_VALUE_SCALE, COORD_VALUE_SCALE, COORD_VALUE_SCALE);
        font.drawInBatch(String.valueOf(value), 10 / COORD_VALUE_SCALE, 0, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, combinedLight);
        poseStack.popPose();
    }

}
