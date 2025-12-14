package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ITargetController;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.satellite.core.SatelliteWeaponsManager;
import com.holybuckets.satellite.menu.TargetControllerMenu;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TargetControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE, ITargetController, Container
{
    private int colorId;
    private int targetColorId;

    private BlockPos uiTargetBlockPos;
    private Vec3 uiCursorPos;
    private BlockPos waypointPos;

    private NonNullList<ItemStack> items;
    private Player playerFiredWeapon;
    private static final Map<Item, BiConsumer<TargetControllerBlockEntity, ItemStack>> weapons = new HashMap<>();

    public static void addWeapon(Item item, BiConsumer<TargetControllerBlockEntity, ItemStack> consumer) {
        weapons.put(item, consumer);
    }
    public static boolean validWeapon(Item item) { return weapons.containsKey(item); }
    public static void removeWeapon(Item item) { weapons.remove(item); }


    public TargetControllerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.targetControllerBlockEntity.get(), pos, state);

        colorId = 0;
        targetColorId = 0;

        this.uiTargetBlockPos = null;
        this.uiCursorPos = null;
        this.waypointPos = null;

        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        this.playerFiredWeapon = null;
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
    public void setColorId(int colorId) {
        this.colorId = colorId;
        markUpdated();
    }

    @Override
    public int getTargetColorId() {
        return targetColorId;
    }


    public void setTargetColorId(int colorId) {
        this.targetColorId = colorId;
        SatelliteWeaponsManager.fireWaypointMessage(this, ItemStack.EMPTY, false);
        markUpdated();
    }

    @Nullable
    public Player getPlayerFiredWeapon() {
        return playerFiredWeapon;
    }

    @Override
    public void toggleOnOff(boolean toggle) {
        this.uiCursorPos = null;
        this.uiTargetBlockPos = null;
        this.waypointPos = null;
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
                targetColorId = SatelliteManager.getColorId(b);
            } else {
                targetColorId = (targetColorId + 1) % SatelliteManager.totalIds();
            }
            setTargetColorId(targetColorId);
            cmd = -1;
        }

        if( isDisplayOn && (source!=null) && (cmd>-1))
            source.sendinput(p, hand, cmd, this);
        
        updateBlockState();
    }

    public void fireWeapon(Player p)
    {
        if(this.level==null || level.isClientSide) return;
        ItemStack stack = this.getItem(0);
        Item item = stack.getItem();
        this.playerFiredWeapon = p;
        BiConsumer<TargetControllerBlockEntity, ItemStack> consumer = weapons.get(item);
        if(consumer != null) {
            consumer.accept(this, stack);
            this.waypointPos = this.getUiTargetBlockPos();
            markUpdated();
        }
    }


    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        tag.putInt("targetColorId", targetColorId);
        if(uiTargetBlockPos != null) {  //saved to send to client for rendering
            String pos = HBUtil.BlockUtil.positionToString(uiTargetBlockPos);
            tag.putString("uiTargetBlockPos", pos);
        } else {
            tag.putString("uiTargetBlockPos", "");
        }
        //Save item and itemMetadata
        if(!this.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.items.get(0).save(itemTag);
            tag.put("itemStack", itemTag);
        }

        //save waypoint
        if(waypointPos != null) {
            String pos = HBUtil.BlockUtil.positionToString(waypointPos);
            tag.putString("waypointPos", pos);
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
        //Load item and itemMetadata
        if(tag.contains("itemStack")) {
            CompoundTag itemTag = tag.getCompound("itemStack");
            ItemStack stack = ItemStack.of(itemTag);
            this.items.set(0, stack);
        }

        if(tag.contains("waypointPos"))
        {
            boolean wpIsNull = (waypointPos==null);
            String str = tag.getString("waypointPos");
            waypointPos = (str.equals("")) ? null :
                new BlockPos( HBUtil.BlockUtil.stringToBlockPos(str) );
            if( wpIsNull && (this.level!=null) && !this.level.isClientSide)
                SatelliteWeaponsManager.fireWaypointMessage(this, ItemStack.EMPTY);
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

    //CONTAINER METHODS
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return weapons.containsKey(stack);
    }

    @Override
    public int getContainerSize() { return 1; }

    @Override
    public boolean isEmpty() { return this.items.get(0).isEmpty(); }

    @Override
    public ItemStack getItem(int i) { return this.items.get(0) ; }

    @Override
    public ItemStack removeItem(int i, int i1) {
     return removeItemNoUpdate(i);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        if(!this.isEmpty()) {
            ItemStack item = this.items.get(0);
            items.set(0, ItemStack.EMPTY);
            return item;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(0, itemStack);
    }


    public boolean stillValid(Player $$0) {
        return Container.stillValidBlockEntity(this, $$0);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public BalmMenuProvider getMenuProvider() {
        return new BalmMenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.hbs_satellites.target_controller_menu");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                TargetControllerBlockEntity.this.setLevel(player.level());
                return new TargetControllerMenu(syncId, playerInventory, TargetControllerBlockEntity.this);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                buf.writeBlockPos(worldPosition);
            }
        };
    }
}
