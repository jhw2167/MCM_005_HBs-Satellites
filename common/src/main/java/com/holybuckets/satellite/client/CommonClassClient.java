package com.holybuckets.satellite.client;

import com.holybuckets.satellite.CommonClass;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.client.core.SatelliteDisplayClient;
import com.holybuckets.satellite.client.screen.ModScreens;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.foundation.client.ClientEventRegistrar;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.client.ConnectedToServerEvent;
import net.blay09.mods.balm.api.event.client.DisconnectedFromServerEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static com.holybuckets.satellite.CommonClass.isViewingHoloBit;
import static com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity.REACH_DIST_BLOCKS;


public class CommonClassClient {

    public static void initClient() {
        ClientEventRegistrar registrar = ClientEventRegistrar.getInstance();
        registrar.registerOnConnectedToServer(CommonClassClient::onConnectedToServer, EventPriority.Highest);
        registrar.registerOnDisconnectedFromServer(CommonClassClient::onDisconnectedFromServer, EventPriority.Lowest);
        SatelliteDisplayClient.init(registrar);

        //ClientBalmEventRegister.registerEvents();


        ModRenderers.clientInitialize(BalmClient.getRenderers());
        ModScreens.clientInitialize(BalmClient.getScreens());
        //ModItems.clientInitialize();
    }

    /**
     * Description: Run sample tests methods
     */
    public static void sample()
    {

    }

    //** EVents
    private static void onConnectedToServer(ConnectedToServerEvent event) {
        SatelliteMain.loadConfig();
        SatelliteManager.onWorldStart();
    }

    private static void onDisconnectedFromServer(DisconnectedFromServerEvent event){
        SatelliteManager.onWorldStop();
    }

    //** Utility

    public static TextureAtlasSprite getSprite(ResourceLocation rl) {
        TextureAtlas textureAtlas = Minecraft.getInstance().getModelManager()
            .getAtlas(InventoryMenu.BLOCK_ATLAS);
        return textureAtlas.getSprite( rl );
    }


    //Render

    //Rendering
    public static void renderUiSphere(Camera camera, PoseStack poseStack)
    {
        if (!(camera.getEntity() instanceof Player player)) {
            return;
        }

        Level level = camera.getEntity().level();
        //if( !SatelliteManager.isAnyControllerOn()) return; pending implementation
        BlockHitResult hitResult = CommonClass.getAnyHitResult(level, player, REACH_DIST_BLOCKS*2);
        if( hitResult == null ) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance()
            .renderBuffers().bufferSource();

        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
        Vec3 cameraPos = camera.getPosition();

        // Attempts to make lines more visible
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);

        poseStack.pushPose();

        // Translate relative to camera
        poseStack.translate(
            -cameraPos.x,
            -cameraPos.y,
            -cameraPos.z
        );

        double bitSize = 1.0 / 16.0; // 1/16th of a block
        int radius = 2;
        // Floor to nearest bit position
        Vec3 hitLocation = hitResult.getLocation();
        double snappedX = Math.floor(hitLocation.x / bitSize) * bitSize;
        double snappedY = Math.floor(hitLocation.y / bitSize) * bitSize;
        double snappedZ = Math.floor(hitLocation.z / bitSize) * bitSize;
        Vec3 center = new Vec3(snappedX, snappedY, snappedZ);

        // Iterate through all bit positions in radius
        LineBuilder BUILDER = new LineBuilder(builder, poseStack);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Check if within sphere
                    if (x*x + y*y + z*z <= radius*radius) {
                        // Calculate bit world position
                        double bitX = center.x + (x * bitSize);
                        double bitY = center.y + (y * bitSize);
                        double bitZ = center.z + (z * bitSize);
                        if( !isViewingHoloBit(level, hitResult, new Vec3(x * bitSize, y * bitSize, z * bitSize) ))continue;
                        if(x==0d && y==0d && z==0d) BUILDER.setColor(LINE_COLOR_RED);
                        else                        BUILDER.setColor(LINE_COLOR);
                        BUILDER.drawCube(bitX, bitY, bitZ, bitSize);

                    }
                }
            }
        }

        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }


    private static final float[] LINE_COLOR = {1.0f, .70f, 0.0f, 1.0f}; // Green RGBA
    private static final float[] LINE_COLOR_WHITE = {1.0f, 1.0f, 1.0f, 1.0f}; // Green RGBA
    private static final float[] LINE_COLOR_RED = {1.0f, 0.0f, 0.0f, 1.0f}; // Red RGBA

    private static class LineBuilder {
        private final VertexConsumer builder;
        private final Matrix4f matrix;
        private final Matrix3f normal;
        private float[] color;

        public LineBuilder(VertexConsumer builder, PoseStack poseStack) {
            this.builder = builder;
            this.matrix = poseStack.last().pose();
            this.normal = poseStack.last().normal();
        }

        void setColor(float[] color) {
            this.color = color;
        }

        public void addLine(double x1, double y1, double z1, double x2, double y2, double z2) {
            builder.vertex(matrix, (float) x1, (float) y1, (float) z1)
                .color(color[0], color[1], color[2], color[3])
                .normal(normal, 1, 0, 0)
                .endVertex();

            builder.vertex(matrix, (float) x2, (float) y2, (float) z2)
                .color(color[0], color[1], color[2], color[3])
                .normal(normal, 1, 0, 0)
                .endVertex();
        }

        public void drawCube(double minX, double minY, double minZ, double size)
        {
            double maxX, maxY, maxZ;
            maxX = minX + size; maxY = minY + size; maxZ = minZ + size;

            // Bottom face (4 edges)
            addLine(minX, minY, minZ, maxX, minY, minZ);
            addLine(maxX, minY, minZ, maxX, minY, maxZ);
            addLine(maxX, minY, maxZ, minX, minY, maxZ);
            addLine(minX, minY, maxZ, minX, minY, minZ);

            // Top face (4 edges)
            addLine(minX, maxY, minZ, maxX, maxY, minZ);
            addLine(maxX, maxY, minZ, maxX, maxY, maxZ);
            addLine(maxX, maxY, maxZ, minX, maxY, maxZ);
            addLine(minX, maxY, maxZ, minX, maxY, minZ);

            // Vertical edges (4 edges)
            addLine(minX, minY, minZ, minX, maxY, minZ);
            addLine(maxX, minY, minZ, maxX, maxY, minZ);
            addLine(maxX, minY, maxZ, maxX, maxY, maxZ);
            addLine(minX, minY, maxZ, minX, maxY, maxZ);
        }
    }




}
