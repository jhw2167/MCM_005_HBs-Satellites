package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.TargetControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ITargetController;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TargetControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE, ITargetController
{
    private int colorId = 0;
    private BlockPos uiTargetBlockPos;
    private Vec3 uiCursorPos;

    public TargetControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.targetControllerBlockEntity.get(), pos, state);
    }

    @Override
    public BlockPos getUiTargetBlockPos() {
        return uiTargetBlockPos;
    }

    @Override
    public void setUiTargetBlockPos(BlockPos blockPos) {
        this.uiTargetBlockPos = blockPos;
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
        updateBlockState();
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

    @Override
    public void toggleOnOff(boolean toggle) {
        this.uiCursorPos = null;
        this.uiTargetBlockPos = null;
        super.toggleOnOff(toggle);
    }

    public SatelliteControllerBlockEntity getSatelliteController() {
        if (source == null) return null;
        return source.getSatelliteController();
    }

    public void use(Player p, InteractionHand hand, BlockHitResult hitResult)
    {
        if(this.level==null || level.isClientSide) return;
        int cmd = ISatelliteControllerBE.calculateHitCommandTarget(hitResult);
        if (cmd == -1) return;

        //has its own wool color subchanel set
        if (cmd == 16) {
            if( p.getItemInHand(hand).getItem() instanceof BlockItem bi ) {
                Block b = bi.getBlock();
                colorId = SatelliteManager.getColorId(b);
            } else {
                colorId = (colorId + 1) % 16;
            }
            cmd = -1;
        }

        if( isDisplayOn && (source!=null) && (cmd>-1))
            source.sendinput(p, hand, cmd, this);
        
        updateBlockState();
    }


    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        if(uiTargetBlockPos != null) {  //saved to send to client for rendering
            String pos = HBUtil.BlockUtil.positionToString(uiTargetBlockPos);
            tag.putString("uiTargetBlockPos", pos);
        } else {
            tag.putString("uiTargetBlockPos", "");
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
        if(tag.contains("uiTargetBlockPos")) {
            String str = tag.getString("uiTargetBlockPos");
            uiTargetBlockPos = (str.equals("")) ? null :
                new BlockPos( HBUtil.BlockUtil.stringToBlockPos(str) );
        }
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
