package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
    protected int ticks;  //tick counter for refresh rate
    private int height; // height of display area to clear
    private int holoLift; //number of blocks above display block holo renders
    private boolean hasPlayer;

    public SatelliteDisplayBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.satelliteDisplayBlockEntity.get(), pos, state);
    }

    public SatelliteDisplayBlockEntity(BlockEntityType<? extends BlockEntity> beType, BlockPos pos, BlockState state) {
        super(beType, pos, state);
        this.isDisplayOn = false;
        this.ticks = new Random(this.hashCode()).nextInt(REFRESH_RATE);
        this.height = 0;
        this.holoLift = 0;
        this.hasPlayer = false;
    }

    public void setHeight(int height) {
        if (height != this.height) {
            this.height = height;
            if (height > 0) {
                clearAboveArea(height);
            }
        }
    }

    private void clearAboveArea(int height) {
        BlockPos pos = this.getBlockPos().above(holoLift);
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
    public void setSource(SatelliteDisplay source, boolean forceUpdate) {
        if(this.level.isClientSide) return;
        if(this.source == source && !forceUpdate) return; //same source

        this.source = source;
        if(this.source == null || source.noSource() ) return;
        this.displayInfo = source.initDisplayInfo(this);

        this.forceUpdate();
    }

    public void buildDisplay()
    {
        BlockPos pos = this.getBlockPos().above(holoLift);
        for(ChunkDisplayInfo info : displayInfo)
        {
            pos = pos.above();
            boolean proceedWithUpdates = false;
            for(boolean b : info.hasUpdates ) {
                if(b) {proceedWithUpdates = true; break;}
            }
            if(proceedWithUpdates) {
                SatelliteMain.chiselBitsApi.build(this.level, info.holoBits, pos, info.hasUpdates);
            }
        }

    }

    public void clearDisplay() {
        if(displayInfo == null) return;
        clearAboveArea(this.source.getDepth());
        displayInfo.forEach( info -> info.isActive = false );
        displayInfo.clear();
    }

    public void onDestroyed() {
        this.clearDisplay();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteDisplayBlockEntity) {

        if(this.level.isClientSide) return;
        if( source == null) return;
        ticks++;

        if(source.noSource() || !source.contains(this.getBlockPos())) {
            this.clearDisplay();
        } else if(displayInfo != null && !displayInfo.isEmpty()) {
            renderDisplay();
        }

    }

    private static final int REFRESH_RATE = 60;
    private static final int PLAYER_REFRESH_RATE = 10;
    private void renderDisplay() {

        if(source.needsClear() ) {
            this.clearAboveArea(this.height);
            height = this.source.getDepth();
        }

        if( ticks % REFRESH_RATE==0) {
            this.displayInfo.forEach( info -> info.refreshBits(false) );
            buildDisplay();
        } else if( this.hasPlayer && ticks % PLAYER_REFRESH_RATE == 0) {
            this.displayInfo.forEach( info -> info.refreshBits(true) );
            buildDisplay();
        }

    }

    //** ENITIY OVERRIDES

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("height", height);
        tag.putInt("holoLift", holoLift);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.height = tag.getInt("height");
        this.holoLift = tag.getInt("holoLift");
        //if(height > 0) clearAboveArea(height);
    }


}
