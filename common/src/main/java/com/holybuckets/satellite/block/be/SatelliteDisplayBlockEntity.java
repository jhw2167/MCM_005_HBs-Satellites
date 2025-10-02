package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Deque;
import java.util.Random;

public class SatelliteDisplayBlockEntity extends BlockEntity implements ISatelliteDisplayBlock, BlockEntityTicker<SatelliteDisplayBlockEntity> {

    protected SatelliteDisplay source;
    protected Deque<ChunkDisplayInfo> displayInfo;
    protected boolean isDisplayOn;
    private int ticks;  //tick counter for refresh rate
    private int height; // height of display area to clear
    private boolean hasPlayer;

    public SatelliteDisplayBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.satelliteDisplayBlockEntity.get(), pos, state);
    }

    public SatelliteDisplayBlockEntity(BlockEntityType<? extends BlockEntity> beType, BlockPos pos, BlockState state) {
        super(beType, pos, state);
        this.isDisplayOn = true;
        this.ticks = new Random(this.hashCode()).nextInt(REFRESH_RATE);
        this.height = 0;
        this.hasPlayer = false;
    }

    public void setHeight(int height) {
        if (height != this.height) {
            this.height = height;
            if (height > 0) {
                clearAboveArea();
            }
        }
    }

    private void clearAboveArea() {
        BlockPos pos = this.getBlockPos();
        for (int y = 1; y <= height; y++) {
            BlockPos clearPos = pos.above(y);
            SatelliteMain.chiselBitsApi.clear(this.level, clearPos);
        }
    }

    @Override
    public void forceUpdate() {
        //Set ticks equal to -x + -z offset
        BlockPos offset = this.source.getOffset(this.getBlockPos());
        this.ticks = -(Math.abs(offset.getX()) + Math.abs(offset.getZ()));
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
        if(this.displayInfo != null && !this.displayInfo.isEmpty()) {
            this.displayInfo.forEach( info -> info.isActive = false );
        }
        this.displayInfo = source.initDisplayInfo(this);

        this.forceUpdate(); //force build over next few ticks
    }

    public void buildDisplay()
    {
        BlockPos pos = getBlockPos();
        this.hasPlayer = false;

        int newHeight = displayInfo.size();
        if(newHeight < height) {
            clearAboveArea(); this.height = newHeight;
        }

        for(ChunkDisplayInfo info : displayInfo)
        {
            pos = pos.above();
            info.isActive = true;
            this.hasPlayer |= info.hasPlayer;

            boolean proceedWithUpdates = false;
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
        if (height > 0) {
            clearAboveArea();
        }
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

    private static final int REFRESH_RATE = 200;
    private static final int PLAYER_REFRESH_RATE = 10;
    private void renderDisplay() {

        if( (ticks++) % REFRESH_RATE==0) {
            //this.displayInfo.forEach( info -> info.refreshBits() );
        } else if(this.hasPlayer && (ticks % PLAYER_REFRESH_RATE==0)) {
            //this.displayInfo.forEach( info -> info.refreshBits() );
        } else {
            return;
        }
        buildDisplay();
    }


}
