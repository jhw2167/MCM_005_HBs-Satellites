package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class SatelliteDisplayBlockEntity extends BlockEntity implements ISatelliteDisplayBlock, BlockEntityTicker<SatelliteDisplayBlockEntity> {

    protected  SatelliteDisplay source;
    protected Deque<ChunkDisplayInfo> displayInfo;
    protected boolean isDisplayOn;
    private int ticks;  //tick counter for refresh rate

    public SatelliteDisplayBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.satelliteDisplayBlockEntity.get(), pos, state);
    }

    public SatelliteDisplayBlockEntity(BlockEntityType<? extends BlockEntity> beType, BlockPos pos, BlockState state) {
        super(beType, pos, state);
        this.isDisplayOn = true;
        this.ticks = new Random(this.hashCode()).nextInt(REFRESH_RATE);
    }

    @Override
    public void toggleOnOff(boolean toggle) {
        this.isDisplayOn = toggle;
    }

    @Override
    public SatelliteDisplay getSource() {
        return this.source;
    }

    @Override
    public void setSource(SatelliteDisplay source) {
        if(this.level.isClientSide) return;
        if(source == null || source.noSource() ) return;
        this.source = source;
         this.displayInfo = source.initDisplayInfo(this);
        //buildDisplay(); during tick loop
    }

    public void buildDisplay() {
        BlockPos pos = getBlockPos();
        for(ChunkDisplayInfo info : displayInfo) {
            pos = pos.above();
            boolean proceedWithUpdates = false;
            info.isActive = true;
            for(boolean b : info.hasUpdates ) {
                if(b) {proceedWithUpdates = true; break;}
            }
            if(!proceedWithUpdates) continue;
            SatelliteMain.chiselBitsApi.build(this.level, info.holoBits, pos, info.hasUpdates);
        }
    }

    public void clearDisplay() {
        if(displayInfo == null) return;
        BlockPos pos = this.getBlockPos();
        for(ChunkDisplayInfo info : displayInfo) {
            pos = pos.above();
            SatelliteMain.chiselBitsApi.clear(this.level, pos);
            info.isActive = false;
        }
        //SatelliteControllerMessage.createAndFire(0,getBlockPos());
    }

    public void onDestroyed() {
        this.clearDisplay();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteDisplayBlockEntity) {

        if (this.level.isClientSide) {
            //BlockEntity be = level.getBlockEntity(blockPos.above());
            return;
        }
        else if( source == null) { return; }

        if(source.noSource() || !source.contains(this.getBlockPos()) || source.getDepth() < displayInfo.size()) {
            clearDisplay();
        } else if(displayInfo != null && !displayInfo.isEmpty()) {
            renderDisplay();
        }

    }

    private static final int REFRESH_RATE = 20;
    private void renderDisplay() {
        if( (ticks++) % REFRESH_RATE==0) {
            buildDisplay();
        }
    }


    @Override
    public void updateClient(SatelliteDisplayUpdate update)
    {
        /**
         * If update.bits is null, remove the block entity at the position, set to air
         * else, create new ChunkDisplayInfo and call SatelliteMain.chiselBitsApi.build, add to Deque
         */
         BlockPos targetPos = update.pos;
         if(targetPos == null) return;
         if(targetPos == getBlockPos()) {
            if(!update.displayOn) clearDisplay();
            return;
         }

         if(update.displayData == null) {
            BlockEntity be = level.getBlockEntity(targetPos);
            //if(be != null) level.removeBlockEntity(targetPos);
            //level.setBlock(targetPos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
         } else {
            ChunkDisplayInfo info = new ChunkDisplayInfo(update.displayData);
            BlockEntity be = SatelliteMain.chiselBitsApi.build(this.level, info.holoBits, targetPos);
            be.setChanged();
         }

    }

}
