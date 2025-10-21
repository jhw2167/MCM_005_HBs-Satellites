package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;

public class SatelliteBlockEntity extends BlockEntity implements ISatelliteBE, BlockEntityTicker<SatelliteBlockEntity>
{
    int colorId;
    LevelChunk currentChunk;
    private static final int HEXCODE_MAX = 0xFFFFFF;

    public SatelliteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteBlockEntity.get(), pos, state);
        //this.setColorId( (int) (Math.random() * HEXCODE_MAX));
        this.colorId = -1;
    }

    public void use(Player p, InteractionHand hand, BlockHitResult res)
    {
        int cmd = 16;
        if (cmd == 16)
        {
            //If player is holding wool in their hand, set tot that color
            if( p.getItemInHand(hand).getItem() instanceof BlockItem bi ) {
                Block b = bi.getBlock();
                int color = SatelliteManager.getColorId(b);
                if(color >= 0) {
                    this.setColorId( color );
                }
            } else {    //Otherwise, cycle to next color
                this.setColorId( (this.colorId + 1) % SatelliteManager.totalIds() );
            }

        }
    }

    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public void setColorId(int colorId) {
        SatelliteManager.remove(this.colorId);
        this.colorId = colorId;
        this.markUpdated();
    }

    public void onDestroyed() {
        SatelliteManager.remove(this.colorId);
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

    public void setLevelChunk(LevelChunk chunk) {
        this.currentChunk = chunk;
    }


    //** Serialization

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
    }

    //** Networking
    private void markUpdated() {
        this.setChanged();
        if(this.level == null) return;
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

}
