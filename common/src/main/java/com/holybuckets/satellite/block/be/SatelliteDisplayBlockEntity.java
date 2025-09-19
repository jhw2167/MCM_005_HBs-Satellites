package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;
import com.holybuckets.satellite.networking.ModNetworking;
import com.holybuckets.satellite.networking.SatelliteDisplayMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Deque;
import java.util.List;

import static net.minecraft.world.level.block.Blocks.AIR;

public class SatelliteDisplayBlockEntity extends BlockEntity implements ISatelliteDisplayBlock, BlockEntityTicker<SatelliteDisplayBlockEntity> {

    protected  SatelliteDisplay source;
    protected Deque<ChunkDisplayInfo> displayInfo;
    protected boolean isDisplayOn;

    public SatelliteDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteDisplayBlockEntity.get(), pos, state);
        this.isDisplayOn = false;
    }

    public SatelliteDisplayBlockEntity(BlockEntityType<? extends BlockEntity> beType, BlockPos pos, BlockState state) {
        super(beType, pos, state);
        this.isDisplayOn = false;
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
        BlockPos pos = this.getBlockPos();
        for(ChunkDisplayInfo info : displayInfo) {
            pos = pos.above();
            BlockEntity be = SatelliteMain.chiselBitsApi.build(this.level, info.holoBits, pos);
            be.setChanged();
            SatelliteDisplayMessage.createAndFire(pos, info.holoBits);
        }
    }

    public void clearSource() {
        this.source = null;
        BlockPos pos = this.getBlockPos();
        for(ChunkDisplayInfo info : displayInfo) {
            pos = pos.above();
            BlockEntity be = level.getBlockEntity(pos);
            if(be != null) {
                level.removeBlockEntity(pos);
            }
            level.setBlock(pos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
        }
        this.displayInfo = null;
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteDisplayBlockEntity) {
        if (this.level.isClientSide) return;
        if( source == null || source.noSource()) return;
        if(source.noSource() || !source.contains(this.getBlockPos())) {
            clearSource();
            return;
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
            if(!update.displayOn) clearSource();
            return;
         }

         if(update.displayData == null) {
            BlockEntity be = level.getBlockEntity(targetPos);
            if(be != null) level.removeBlockEntity(targetPos);
            level.setBlock(targetPos, AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE );
         } else {
            ChunkDisplayInfo info = new ChunkDisplayInfo(update.displayData);
            BlockEntity be = SatelliteMain.chiselBitsApi.build(this.level, info.holoBits, targetPos);
            be.setChanged();
         }

    }

}
