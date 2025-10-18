package com.holybuckets.satellite.client;

import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

public class SatelliteControllerRenderer implements BlockEntityRenderer<SatelliteControllerBlockEntity> {

    public SatelliteControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SatelliteControllerBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        BlockState blockState = blockEntity.getBlockState();
        Direction facing = Direction.NORTH; // Get actual facing from blockstate when implemented
        
        // Save the current transformation state
        poseStack.pushPose();

        // Translate to block center
        poseStack.translate(0.5D, 0.5D, 0.5D);
        
        // Rotate based on facing direction
        switch(facing) {
            case SOUTH:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                break;
            case WEST:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                break;
            case EAST:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
                break;
            default:
                break;
        }

        // Translate back
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        // Get the vertex builder for the render type you want
        VertexConsumer builder = bufferSource.getBuffer(RenderType.translucent());

        // Example: Render a colored overlay on the front face
        Matrix4f matrix = poseStack.last().pose();
        float alpha = 0.8f; // Transparency
        float red = 1.0f;   // Example color values
        float green = 0.0f;
        float blue = 0.0f;

        // Front face (adjust Z position as needed)
        builder.vertex(matrix, 0, 0, 0.01f).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, 1, 0, 0.01f).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, 1, 1, 0.01f).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, 0, 1, 0.01f).color(red, green, blue, alpha).endVertex();

        // Restore the transformation state
        poseStack.popPose();
    }
}
