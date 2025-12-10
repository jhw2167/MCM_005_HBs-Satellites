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
    private int colorId;
    private BlockPos uiPosition;
    private SatelliteItemUpgrade[] upgrades;

    public UpgradeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.upgradeControllerBlockEntity.get(), pos, state);
        upgrades = new SatelliteItemUpgrade[4];
        colorId = 0;
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
        if(this.level==null) return;
        int cmd = ISatelliteControllerBE.calculateHitCommandUpgrade(hitResult);
        if (cmd == -1) return;

        if(source != null) {
            source.sendinput(player, hand, cmd);
            setUpgrades(source.getUpgrades());
        }

        updateBlockState();
    }

    private void setUpgrades(SatelliteItemUpgrade[] upgrades) {
        if(upgrades == null) return;
        for(int i = 0; i < this.upgrades.length; i++) {
            this.upgrades[i] = upgrades[i];
        }
    }

    @Override
    protected void updateBlockState() {
        super.updateBlockState();
        this.markUpdated();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        
        // Save upgrades array
        for(int i = 0; i < upgrades.length; i++) {
            tag.putString(UPGRADE_KEY+i, upgradeString(upgrades[i]));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
         colorId = tag.getInt("colorId");
        
        // Load upgrades array
        for(int i = 0; i < upgrades.length; i++) {
           String upgString = tag.getString(UPGRADE_KEY+i);
           ResourceLocation upgRL = ResourceLocation.tryParse(upgString);
           if(upgRL == null || !BuiltInRegistries.ITEM.containsKey(upgRL)) {
               upgrades[i] = null;
               continue;
           }
            upgrades[i] = (SatelliteItemUpgrade) BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(upgString));
            if(source != null) {
                source.addUpgrade(upgrades[i], i);
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


    //** Statics
    private static final String UPGRADE_KEY = "SatelliteItemUpgrade";
    private static String upgradeString(SatelliteItemUpgrade upgrade) {
        if (upgrade == null) return "";
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(upgrade);
        return itemId.toString();
    }

}
