package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.block.HeightControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.core.SatelliteDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class HeightControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE {
    private int colorId = 0;
    private BlockPos uiPosition = BlockPos.ZERO;

    public HeightControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.heightControllerBlockEntity.get(), pos, state);
    }

    @Override
    public BlockPos getUiPosition() {
        return uiPosition;
    }

    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public void setColorId(int colorId) {
        this.colorId = colorId;
        markUpdated();
    }

    @Override
    public void setSource(SatelliteDisplay source, boolean forceDisplayUpdates) {
        // Simple controllers don't manage sources
    }

    public void use(Player player, InteractionHand hand, BlockHitResult hitResult) {
        int cmd = ISatelliteControllerBE.calculateHitCommand(hitResult);
        if (cmd == -1) return;
        
        // Handle basic color changing
        if (cmd == 16) {
            setColorId((getColorId() + 1) % 16);
        }
        
        updateBlockState();
    }

    private void updateBlockState() {
        if (this.level == null) return;
        BlockState state = this.getBlockState();
        BlockState newState = state.setValue(HeightControllerBlock.POWERED, this.colorId > 0);
        level.setBlock(this.getBlockPos(), newState, 3);
    }

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

    private void markUpdated() {
        this.setChanged();
        if (this.level == null) return;
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
