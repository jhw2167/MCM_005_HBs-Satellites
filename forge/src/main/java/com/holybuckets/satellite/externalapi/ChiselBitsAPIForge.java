package com.holybuckets.satellite.externalapi;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.api.ChiselBitsAPI;
import com.holybuckets.satellite.block.HoloBaseBlock;
import com.holybuckets.satellite.block.HoloBlock;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.util.IBatchMutation;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.multistate.mutator.ChiselAdaptingWorldMutator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

import static com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity.REACH_DIST_BLOCKS;
import static net.minecraft.world.level.block.Blocks.AIR;

public class ChiselBitsAPIForge implements ChiselBitsAPI {

    static final IBlockInformation[] HOLO_BLOCKS = new IBlockInformation[16];

    private static void initHolo(Level level)
    {
        if(HOLO_BLOCKS[0] != null) return;

        //GetBlock Entity from registry
        /*
        ChiseledBlockEntity CHISELED = level.registryAccess()
            .registryOrThrow(Registries.BLOCK_ENTITY_TYPE)
            .get(new ResourceLocation(Constants.MOD_ID_CHISELED_BITS, "chiseled_block")).
        */
        Optional<BlockEntity> CHISELED = Optional.empty();
        BlockState AIR = ChiselBitsAPI.HOLO_EMPTY().defaultBlockState();
        HOLO_BLOCKS[0] = new BlockInformation( AIR, IStateVariantManager.getInstance()
            .getStateVariant(AIR, CHISELED)
        );

        BlockState LIGHT = ChiselBitsAPI.HOLO_LIGHT().defaultBlockState();
        HOLO_BLOCKS[1] = new BlockInformation( LIGHT,
            IStateVariantManager.getInstance().getStateVariant(LIGHT, Optional.empty())
        );

        BlockState BASE = ChiselBitsAPI.HOLO_BASE().defaultBlockState();
        HOLO_BLOCKS[2] = new BlockInformation( BASE,
            IStateVariantManager.getInstance().getStateVariant(BASE, CHISELED)
        );

        BlockState DARK = ChiselBitsAPI.HOLO_DARK().defaultBlockState();
        HOLO_BLOCKS[3] = new BlockInformation( DARK,
            IStateVariantManager.getInstance().getStateVariant(DARK, CHISELED)
        );

        BlockState BLACK = ChiselBitsAPI.HOLO_BLACK().defaultBlockState();
        HOLO_BLOCKS[4] = new BlockInformation( BLACK,
            IStateVariantManager.getInstance().getStateVariant(BLACK, CHISELED)
        );

        //Fill with [4] up to index 6
        for(int i = 5; i < ChiselBitsAPI.DEMARCATOR_START_IDX; i++) HOLO_BLOCKS[i] = HOLO_BLOCKS[4];

        //Fill with stained glass colors up to 15
        for(int i = ChiselBitsAPI.DEMARCATOR_START_IDX; i < 16; i++) {
            BlockState STAINED = ChiselBitsAPI.DEMARCATOR(i-8).defaultBlockState();
            HOLO_BLOCKS[i] = new BlockInformation( STAINED,
                IStateVariantManager.getInstance().getStateVariant(STAINED, CHISELED)
            );
        }
    }

    @Override
    public BlockEntity build(Level level, int[] bits, BlockPos pos) {
        return build(level, bits, pos, null);
    }

    @Override
    public BlockEntity build(Level level, int[] bits, BlockPos pos, boolean[] yLevelHasUpdates)
    {
        initHolo(level);
        BlockState above = level.getBlockState(pos);
        BlockEntity aboveBe = level.getBlockEntity(pos);
        boolean aboveIsAir = above.equals(AIR.defaultBlockState());
        boolean aboveIsChiseled = aboveBe instanceof IMultiStateBlockEntity;
        if(!aboveIsAir && !aboveIsChiseled) { return null; }

        ChiselAdaptingWorldMutator mutator = new ChiselAdaptingWorldMutator(level, pos);
        try(IBatchMutation m = mutator.batch() ) {
                for(int y = 0; y < 16; y++) {
                    if(yLevelHasUpdates != null && !yLevelHasUpdates[y]) continue;
                    for(int x = 0; x < 16; x++) {
                        for(int z = 0; z < 16; z++) {
                            mutator.overrideInAreaTarget(
                                HOLO_BLOCKS[ bits[ISatelliteDisplayBlock.getCachePos(x,y,z)] ],
                                ISatelliteDisplayBlock.get3Dpos(x,y,z)
                            );
                        }
                    }
                    if(yLevelHasUpdates != null) yLevelHasUpdates[y] = false;
                }
        } catch (Exception e) {
            LoggerProject.logError ("100003","Error placing chiseled block at " + pos + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        //return  blockEntity;
        return level.getBlockEntity(pos);
    }
    @Override
    public void clear(Level level, BlockPos pos) {
        clear(level, pos, -1);
    }

    @Override
    public void clear(Level level, BlockPos pos, int yLevel)
    {

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if( yLevel < 0 || yLevel > 15) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
        } else if(!(blockEntity instanceof ChiseledBlockEntity)) {
                level.setBlock(pos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
                level.removeBlockEntity(pos);
        } else {
            ChiselAdaptingWorldMutator mutator = new ChiselAdaptingWorldMutator(level, pos);
            try(IBatchMutation m = mutator.batch() ) {
                for(int x = 0; x < 16; x++) {
                    for(int z = 0; z < 16; z++) {
                        mutator.clearInAreaTarget( ISatelliteDisplayBlock.get3Dpos(x,yLevel,z) );
                    }
                }
            } catch (Exception e) {
            }
        }

    }

    @Override
    public void update(ChunkDisplayInfo info, int[] bits, List<HBUtil.TripleInt> updates, BlockPos pos) {

    }

    @Override
    public void offset(ChunkDisplayInfo info, int[] bits, List<int[][][]> adj, HBUtil.TripleInt offset, BlockPos pos) {

    }


    /**
     *
     * @param level
     * @param center
     * @param area
     * @param colors
     */
    @Override
    public void highlightArea(Level level, BlockPos center, Vec3[] area, int[] colors)
    {

        //Generates positions in multiple different chiseled blocks
        // to do later maybe
        //ChiselAdaptingWorldMutator mutator = new ChiselAdaptingWorldMutator(level, pos);
        ChiselAdaptingWorldMutator[][][] mutatorArr = new ChiselAdaptingWorldMutator[3][3][3];
        //generator a mutator entry for each block around the center, if its a holo block
        for(int y = -1; y <= 1; y++) {
            for(int x = -1; x <= 1; x++) {
                for(int z = -1; z <= 1; z++) {
                    BlockPos pos = center.offset(x,y,z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if(be != null && be instanceof IMultiStateBlockEntity) {
                        mutatorArr[y+1][z+1][z+1] = new ChiselAdaptingWorldMutator(level, pos);
                    }
                }
            }
        }

        final HBUtil.TripleInt centerIdx = new HBUtil.TripleInt(1,1,1);
        HBUtil.TripleInt idx = new HBUtil.TripleInt(1,1,1);
        try {
            for(int i = 0; i < area.length; i++)
            {
                idx.x = centerIdx.x;
                idx.y = centerIdx.y;
                idx.z = centerIdx.z;
                int dx=0, dy=0, dz=0;


                if(area[i].x < 0) {idx.x = 0; dx=1;}
                else if(area[i].x >= 1) {idx.x = 2; dx=-1;}
                if(area[i].y < 0) {idx.y = 0; dy=1;}
                else if(area[i].y >= 1) {idx.y = 2; dy=-1;}
                if(area[i].z < 0) {idx.z = 0; dz=1;}
                else if(area[i].z >= 1) {idx.z = 2; dz=-1;}

                ChiselAdaptingWorldMutator mutator = mutatorArr[idx.y][idx.x][idx.z];
                if(mutator == null) continue;
                Vec3 areaPrime = area[i].add(dx,dy,dz);

                if(!( mutator.getInAreaTarget( areaPrime )).isPresent()) continue;
                BlockState curr = mutator.getInAreaTarget( areaPrime ).get().getBlockInformation().getBlockState();
                if(curr == null || curr.getBlock() == AIR || curr.getBlock() == ModBlocks.holoAirBlock) continue;
                mutator.overrideInAreaTarget( HOLO_BLOCKS[ colors[i] ],  areaPrime );
            }
        } catch (Exception e) {
            LoggerProject.logError ("100003","Error placing chiseled block at " + center + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isViewingHoloBit(Level level, BlockHitResult hitResult, Vec3 offset) {

        Vec3 target = hitResult.getLocation().add( offset );
        BlockPos newPos = new BlockPos( (int) target.x, (int) target.y, (int) target.z );
        return isViewingHoloBlock(level, newPos, target);
    }

    @Override
    public boolean isViewingHoloBlock(Level level, BlockPos pos, Vec3 loc) {

        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof ChiseledBlock)) return false;
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof IMultiStateBlockEntity cbe)) return false;
        Vec3 target = ChiselBitsAPI.clamp(loc, pos );
        if(!(cbe.isInside(target))) return false;
        BlockState internalState = cbe.getInAreaTarget(target)
            .get().getBlockInformation().getBlockState();

        return (internalState.getBlock() instanceof HoloBlock);
    }

    @Override
    public boolean isViewingHoloBlock(Level level, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Vec3 loc = hitResult.getLocation();
        return isViewingHoloBlock(level, pos, loc);
    }


    //END BUILD


    //Rendering
    @Override
    public void renderUiSphere(Camera camera, PoseStack poseStack)
    {
        if (!(camera.getEntity() instanceof Player player)) {
            return;
        }

        BlockHitResult hitResult = (BlockHitResult) player.pick(REACH_DIST_BLOCKS, 0.5f, true);
        Level level = camera.getEntity().level();
        if (hitResult.getType() != HitResult.Type.BLOCK) return;
        if( !isViewingHoloBlock(level, hitResult) ) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance()
            .renderBuffers().bufferSource();

        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
        Vec3 cameraPos = camera.getPosition();

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
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Check if within sphere
                    if (x*x + y*y + z*z <= radius*radius) {
                        // Calculate bit world position
                        double bitX = center.x + (x * bitSize);
                        double bitY = center.y + (y * bitSize);
                        double bitZ = center.z + (z * bitSize);
                        if( !isViewingHoloBit(level, hitResult, new Vec3(x * bitSize, y * bitSize, z * bitSize) ))
                            continue;
                        LineBuilder BUILDER = new LineBuilder(builder, poseStack, bitX, bitY, bitZ, bitSize);
                        BUILDER.drawCube();

                    }
                }
            }
        }

        poseStack.popPose();

        bufferSource.endBatch(RenderType.lines());

    }

    private static final float[] LINE_COLOR = {1.0f, .60f, 0.0f, 1.0f}; // Green RGBA

    private static class LineBuilder {
        private final VertexConsumer builder;
        private final Matrix4f matrix;
        private final Matrix3f normal;
        private final double minX, minY, minZ, maxX, maxY, maxZ;

        public LineBuilder(VertexConsumer builder, PoseStack poseStack,
                           double minX, double minY, double minZ, double size) {
            this.builder = builder;
            this.matrix = poseStack.last().pose();
            this.normal = poseStack.last().normal();
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = minX + size; this.maxY = minY + size; this.maxZ = minZ + size;
        }

        public void addLine(double x1, double y1, double z1, double x2, double y2, double z2) {
            builder.vertex(matrix, (float) x1, (float) y1, (float) z1)
                .color(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3])
                .normal(normal, 1, 0, 0)
                .endVertex();

            builder.vertex(matrix, (float) x2, (float) y2, (float) z2)
                .color(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3])
                .normal(normal, 1, 0, 0)
                .endVertex();
        }

        public void drawCube() {
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
