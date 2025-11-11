package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.block.TargetControllerBlock;
import com.holybuckets.satellite.block.UpgradeControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.item.SatelliteItemUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Set;

public class UpgradeControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE {
    private int colorId = 0;
    private BlockPos uiPosition = BlockPos.ZERO;
    private SatelliteItemUpgrade[] upgrades = new SatelliteItemUpgrade[4];

    public UpgradeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.upgradeControllerBlockEntity.get(), pos, state);
    }

    @Override
    public BlockPos getUiTargetBlockPos() {
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

    public SatelliteItemUpgrade[] getUpgrades() {
        if(this.source == null) return upgrades;
        return source.getUpgrades();
    }

    @Override
    public SatelliteControllerBlockEntity getSatelliteController() {
        if (source == null) return null;
        return source.getSatelliteController();
    }

    public void use(Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if(this.level==null || level.isClientSide) return;
        int cmd = ISatelliteControllerBE.calculateHitCommandUpgrade(hitResult);
        if (cmd == -1) return;

        if(source != null)
            source.sendinput(player, hand, cmd);
        
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
        
        // Save upgrades array
        for (int i = 0; i < upgrades.length; i++) {
            if (upgrades[i] != null) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(upgrades[i]);
                tag.putString("upgrade_" + i, itemId.toString());
            } else {
                tag.putString("upgrade_" + i, "");
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
        
        // Load upgrades array
        for (int i = 0; i < upgrades.length; i++) {
            String upgradeString = tag.getString("upgrade_" + i);
            if (!upgradeString.isEmpty()) {
                ResourceLocation itemId = new ResourceLocation(upgradeString);
                Item item = BuiltInRegistries.ITEM.get(itemId);
                if (item instanceof SatelliteItemUpgrade) {
                    upgrades[i] = (SatelliteItemUpgrade) item;
                } else {
                    upgrades[i] = null;
                }
            } else {
                upgrades[i] = null;
            }
        }
        
        // Add upgrades to source if source is not null
        if (source != null) {
            for (SatelliteItemUpgrade upgrade : upgrades) {
                if (upgrade != null) {
                    source.addUpgrade(upgrade);
                }
            }
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
