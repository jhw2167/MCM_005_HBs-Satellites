package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteBlockEntity;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SatelliteBlockEntity extends BlockEntity implements ISatelliteBlockEntity, BlockEntityTicker<SatelliteBlockEntity>
{
    int colorId;
    private static final int HEXCODE_MAX = 0xFFFFFF;

    public SatelliteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteBlockEntity.get(), pos, state);
        //this.setColorId( (int) (Math.random() * HEXCODE_MAX));
        this.setColorId(0);
    }

    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public void setColorId(int colorId) {
        SatelliteManager.remove(this.colorId);
        this.colorId = colorId;
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.satelliteBlockEntity.get();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteBlockEntity satelliteBlockEntity) {
        if (this.level.isClientSide) return;
        SatelliteManager.put(this.colorId, this);
    }
}
