package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.block.TargetControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ITargetController;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TargetControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE, ITargetController
{
    private int colorId = 0;
    private BlockPos uiPosition = BlockPos.ZERO;
    private Vec3 uiCursorPos = null;

    public TargetControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.targetControllerBlockEntity.get(), pos, state);
    }

    @Override
    public BlockPos getUiPosition() {
        return uiPosition;
    }

    @Override
    public void setUiPosition(BlockPos blockPos) {
        this.uiPosition = blockPos;
        markUpdated();
    }

    @Nullable
    @Override
    public Vec3 getCursorPosition() {
        return uiCursorPos;
    }

    @Nullable
    @Override
    public void setCursorPosition(Vec3 pos) {
        this.uiCursorPos = pos;
    }


    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public int getTargetColorId() {
        return colorId;
    }

    @Override
    public void setColorId(int colorId) {
        this.colorId = colorId;
        markUpdated();
    }

    public SatelliteControllerBlockEntity getSatelliteController() {
        if (source == null) return null;
        return source.getSatelliteController();
    }

    public void use(Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if(this.level==null || level.isClientSide) return;
        int cmd = ISatelliteControllerBE.calculateHitCommandTarget(hitResult);
        if (cmd == -1) return;

        if(isDisplayOn && source != null)
            source.sendinput(player, hand, cmd, this);
        
        updateBlockState();
    }

    private void updateBlockState() {
        if (this.level == null) return;
        BlockState state = this.getBlockState();
        BlockState newState = state.setValue(TargetControllerBlock.POWERED, this.isDisplayOn);
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
