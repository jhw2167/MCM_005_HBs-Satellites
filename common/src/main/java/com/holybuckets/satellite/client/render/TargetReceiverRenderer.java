package com.holybuckets.satellite.client.render;

import com.holybuckets.satellite.block.be.TargetReceiverBlockEntity;
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

public class TargetReceiverRenderer implements BlockEntityRenderer<TargetReceiverBlockEntity> {

    private Font font;

    public TargetReceiverRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @Override
    public void render(TargetReceiverBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        poseStack.pushPose();

        // Get texture
        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        float minX = 0.46f; // Moved left from 0.48f
        float maxX = 0.53f; // Moved left from 0.54f
        float minY = 0.15f;
        float maxY = 0.22f; // Made 10% taller: (0.21 - 0.16) * 1.1 + 0.16 = 0.215f

        WoolQuad targetChannel = new WoolQuad( blockEntity.getTargetColorId(), minX, maxX, minY, maxY );
        renderWoolChannelTexture(targetChannel, poseStack, bufferSource, facing,light, overlay);

        // Same size and Y position as SatelliteControllerRenderer, but offset to the right
        minX = 0.52f;  // 0.34f + 0.33f offset
        maxX = 0.85f;  // 0.66f + 0.33f offset
        minY = 0.08f;  // Same as SatelliteControllerRenderer
        maxY = 0.22f;  // Same as SatelliteControllerRenderer
        WoolQuad mainChannel = new WoolQuad( blockEntity.getColorId(), minX, maxX, minY, maxY );
        renderWoolChannelTexture(mainChannel, poseStack, bufferSource, facing,light, overlay);

        renderTargetInfo(blockEntity, partialTick, poseStack, bufferSource, light, packedOverlay);

        poseStack.popPose();
    }


    private record WoolQuad(int colorId, float minX, float maxX, float minY, float maxY) {}

    private void renderWoolChannelTexture(WoolQuad quad, PoseStack poseStack,
                                          MultiBufferSource bufferSource, Direction facing,
                                          int light, int overlay)
    {
        if (quad.colorId == -1) return;
        poseStack.pushPose();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());

        // Get wool texture
        ResourceLocation woolLoc = SatelliteManager.getResourceForColorId(quad.colorId);
        TextureAtlasSprite woolSprite = CommonClassClient.getSprite(woolLoc);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float u0 = woolSprite.getU0();
        float v0 = woolSprite.getV0();
        float u1 = woolSprite.getU1();
        float v1 = woolSprite.getV1();

        float offset = 0.01f;

        // Transform based on facing direction
        switch (facing) {
            case NORTH -> {
                builder.vertex(matrix, quad.maxX, quad.minY, -offset)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                builder.vertex(matrix, quad.minX, quad.minY, -offset)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                builder.vertex(matrix, quad.minX, quad.maxY, -offset)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
                builder.vertex(matrix, quad.maxX, quad.maxY, -offset)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, -1).endVertex();
            }
            case SOUTH -> {
                builder.vertex(matrix, quad.minX, quad.minY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                builder.vertex(matrix, quad.maxX, quad.minY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                builder.vertex(matrix, quad.maxX, quad.maxY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
                builder.vertex(matrix, quad.minX, quad.maxY, 1 + offset)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
            }
            case WEST -> {
                builder.vertex(matrix, -offset, quad.minY, quad.minX)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                builder.vertex(matrix, -offset, quad.minY, quad.maxX)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                builder.vertex(matrix, -offset, quad.maxY, quad.maxX)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
                builder.vertex(matrix, -offset, quad.maxY, quad.minX)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, -1, 0, 0).endVertex();
            }
            case EAST -> {
                builder.vertex(matrix, 1 + offset, quad.minY, quad.maxX)
                    .color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                builder.vertex(matrix, 1 + offset, quad.minY, quad.minX)
                    .color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                builder.vertex(matrix, 1 + offset, quad.maxY, quad.minX)
                    .color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
                builder.vertex(matrix, 1 + offset, quad.maxY, quad.maxX)
                    .color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(normal, 1, 0, 0).endVertex();
            }
        } //END SWITCH

        poseStack.popPose();
    }

    static int textColor = 0x000000;

    // Font scale configuration
    private static final float COORD_LABEL_SCALE = 1.2f;
    private static final float COORD_VALUE_SCALE = 0.9f;


    // Y grows downward since we flipped Z earlier
    static float rowHeight = 23.5f;
    static float startY = 6.1f; // start higher on screen (negative moves up visually)
    static int labelMarginAdjust = -32;

    // Button positioning configuration - raised higher on the block
    private static final float BUTTON_Y_OFFSET = (rowHeight * 4)-10;
    private static final float TARGET_BUTTON_X_OFFSET = -31f; // Moved 20% right from -33f: -33 * 0.8 = -26.4f
    private static final float FIRE_BUTTON_X_OFFSET = 32f; // Moved 20% right from 30f


    private void renderTargetInfo(TargetReceiverBlockEntity blockEntity, float partialTick,
                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                 int combinedLight, int combinedOverlay)
    {
        // Get the primary controller to retrieve satellite position
        //SatelliteControllerBlockEntity mainController = blockEntity.getSatelliteController();
        BlockPos targetPos = BlockPos.ZERO;
        if (blockEntity.getUiTargetBlockPos() != null) {
            targetPos = blockEntity.getUiTargetBlockPos();
        }

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
        float scale = 0.008f; // Reverted to original font size for coordinates
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

    private void renderChannelColors(TargetReceiverBlockEntity blockEntity, float partialTick,
                                     PoseStack poseStack, MultiBufferSource bufferSource,
                                     int combinedLight, int combinedOverlay)
    {
        // Buttons below coordinates - using smaller font scale
        float buttonY = startY + BUTTON_Y_OFFSET;
        float buttonScale = 1; // Scale down buttons relative to coordinate font

       //May render linking data
    }


}
