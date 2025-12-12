package com.holybuckets.satellite.menu;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TargetControllerMenu extends AbstractContainerMenu {

    private final Container blockEntity;
    private static final int CONTAINER_COLUMNS = 9;

    public TargetControllerMenu(int syncId, Inventory playerInventory, Container blockEntity) {
        super(ModMenus.targetControllerMenu.get(), syncId);
        this.blockEntity = blockEntity;
        
        // Single weapon slot in center (slot index 0)
        this.addSlot(new WeaponSlot(blockEntity, 0, 80, 35));

        // Player inventory (3 rows)
        int playerInvStartY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < CONTAINER_COLUMNS; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvStartY + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < CONTAINER_COLUMNS; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvStartY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity != null && blockEntity instanceof  BlockEntity be) {
            BlockPos pos = be.getBlockPos();
            return HBUtil.BlockUtil.inRange(pos, player.blockPosition(), 64);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack = slot.getItem();
            ItemStack original = itemstack.copy();

            if (index == 0) {
                // From weapon slot to player inventory
                if (!this.moveItemStackTo(itemstack, 1, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                // From player inventory to weapon slot
                if (!this.moveItemStackTo(itemstack, 0, 1, false))
                    return ItemStack.EMPTY;
            }

            if (itemstack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            return original;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.blockEntity.stopOpen(player);
    }

    private class WeaponSlot extends Slot {
        public WeaponSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return TargetControllerBlockEntity.validWeapon(stack.getItem());
        }
    }
}
