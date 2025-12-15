package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.satellite.core.SatelliteWeaponsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TargetReceiverBlockEntity extends BlockEntity
{
    private int colorId;
    private int targetColorId;
    private BlockPos uiTargetBlockPos;
    private Player playerFiredWeapon;
    
    private static final Map<Item, BiConsumer<TargetReceiverBlockEntity, ItemStack>> weapons = new HashMap<>();

    public static void addWeapon(Item item, BiConsumer<TargetReceiverBlockEntity, ItemStack> consumer) {
        weapons.put(item, consumer);
    }
    public static boolean validWeapon(Item item) { return weapons.containsKey(item); }
    public static void removeWeapon(Item item) { weapons.remove(item); }

    public TargetReceiverBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.targetReceiverBlockEntity.get(), pos, state);
        
        colorId = 0;
        targetColorId = 0;
        this.uiTargetBlockPos = null;
        this.playerFiredWeapon = null;
    }

    public BlockPos getUiTargetBlockPos() {
        return uiTargetBlockPos;
    }

    public void setUiTargetBlockPos(BlockPos blockPos) {
        this.uiTargetBlockPos = blockPos;
        markUpdated();
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
        markUpdated();
    }

    public int getTargetColorId() {
        return targetColorId;
    }

    public void setTargetColorId(int colorId) {
        this.targetColorId = colorId;
        markUpdated();
    }

    public Player getPlayerFiredWeapon() {
        return playerFiredWeapon;
    }

    public void use(Player p, InteractionHand hand, BlockHitResult hitResult)
    {
        if(this.level == null || level.isClientSide) return;
        
        // Handle target color setting similar to TargetControllerBlockEntity
        if( p.getItemInHand(hand).getItem() instanceof BlockItem bi ) {
            Block b = bi.getBlock();
            targetColorId = SatelliteManager.getColorId(b);
        } else {
            targetColorId = (targetColorId + 1) % SatelliteManager.totalIds();
        }
        setTargetColorId(targetColorId);
    }

    public void fireWeapon(Player p)
    {
        if(this.level == null || level.isClientSide) return;
        this.playerFiredWeapon = p;
        
        // Fire weapon logic - simplified since no inventory
        // This would need to be adapted based on how weapons work without inventory
        SatelliteWeaponsManager.fireWaypointMessage(this, ItemStack.EMPTY);
        markUpdated();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        tag.putInt("targetColorId", targetColorId);
        if(uiTargetBlockPos != null) {
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
        targetColorId = tag.getInt("targetColorId");

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
