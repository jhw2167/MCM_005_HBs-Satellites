package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.ModBlocks;
import com.holybuckets.satellite.block.SatelliteDisplayBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Deque;
import java.util.Random;

public class SatelliteDisplayBlockEntity extends BlockEntity implements ISatelliteDisplayBE, BlockEntityTicker<SatelliteDisplayBlockEntity> {

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
    public void toggleOnOff(boolean toggle)
    {
        if(toggle != this.isDisplayOn) {
            this.isDisplayOn = toggle;
            updateBlockState();

            if(!this.isDisplayOn) {
                this.clearDisplay();
            }
        }
    }

    protected void updateBlockState() {
        if(this.level == null) return;
        BlockState state = this.getBlockState();
        BlockState newState = state.setValue(SatelliteDisplayBlock.POWERED, this.isDisplayOn);
        level.setBlock(this.getBlockPos(), newState, 3);
        this.setChanged();
        level.sendBlockUpdated(this.getBlockPos(), state, newState, 3);
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isDisplayOn() {
        return this.isDisplayOn;
    }

    @Override
    public boolean hasPlayer() {
        return this.hasPlayer;
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
        if(this.source == null || source.noSource() ) {
            toggleOnOff(false); return;
        }
        this.toggleOnOff(true);
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
                BlockState before = level.getBlockState(pos);
                int[] holoBits = (source.hasUpgrade(ModItems.oreScannerUpgrade))
                    ? info.getOreScanBits() : info.getHoloBits();
                BlockEntity be = SatelliteMain.chiselBitsApi.build(
                    this.level, holoBits, pos, info.hasUpdates);
                if( be != null ) {
                    level.sendBlockUpdated(pos, before, be.getBlockState(), 11 );
                }
            }
        }

    }

    public void clearDisplay() {

        if(displayInfo == null) {
            this.clearAboveArea(this.height);
            this.height = 0;
            return;
        }
        displayInfo.forEach( info -> info.isActive = false );
        clearAboveArea(displayInfo.size());
        displayInfo.clear();
    }

    public void onDestroyed() {
        this.clearDisplay();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteDisplayBlockEntity) {
        ticks++;
        if(this.level.isClientSide) return;

        if( source==null || source.noSource() || !source.contains(this.getBlockPos())) {
            toggleOnOff(false);
        } else if(displayInfo != null && !displayInfo.isEmpty()) {
            renderDisplay();
        }

    }

    public static int REFRESH_RATE = 60;
    public static int PLAYER_REFRESH_RATE = 10;
    protected void renderDisplay()
    {
        if(source == null || source.noSource()) return;

        if(source.needsClear() ) {
            this.clearAboveArea(this.height);
            height = this.source.getDepth();
        }

        if(displayInfo == null || displayInfo.isEmpty()) return;
        boolean stdRefresh = ticks % REFRESH_RATE==0;
        boolean playerRefresh = this.hasPlayer && ticks % PLAYER_REFRESH_RATE==0;

        if(playerRefresh && stdRefresh) {
            this.displayInfo.forEach( info -> info.refreshBits(true) );
            this.clearAboveArea(this.height);
        } else if(playerRefresh) {
            this.displayInfo.forEach( info -> info.refreshBits(true) );
        } else if (stdRefresh) {
            this.displayInfo.forEach( info -> info.refreshBits(false) );
        } else {
            return;
        }
        this.buildDisplay();
    }

    //** ENITIY OVERRIDES

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("height", height);
        tag.putInt("holoLift", holoLift);
        tag.putBoolean("isDisplayOn", isDisplayOn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.height = tag.getInt("height");
        this.holoLift = tag.getInt("holoLift");
        this.isDisplayOn = tag.getBoolean("isDisplayOn");
        //if(height > 0) clearAboveArea(height);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }


}
