package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.block.be.isatelliteblocks.IHologramDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SatelliteDisplayBlockEntity extends BlockEntity implements IHologramDisplayBlock {
    private ChunkDisplayInfo displayInfo;
    private boolean isToggled;

    public SatelliteDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteDisplayBlockEntity.get(), pos, state);
        this.isToggled = false;
    }

    @Override
    public void setDisplayInfo(ChunkDisplayInfo displayInfo) {
        this.displayInfo = displayInfo;
    }

    @Override
    public void toggleOnOff(boolean toggle) {
        this.isToggled = toggle;
    }

    public void tick() {
        if (level == null || !isToggled || displayInfo == null) return;
        
        // Implementation for display logic will go here
        // This will be called every tick when the block is active
    }
}
