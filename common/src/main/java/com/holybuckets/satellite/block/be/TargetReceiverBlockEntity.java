package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.SatelliteDisplayBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.core.SatelliteEventManager;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    private SatelliteManager manager;
    private TargetControllerBlockEntity linkedTargetController;
    
    private static final Map<BlockEntityType, BiConsumer<TargetReceiverBlockEntity, BlockEntity>> neighborWeapons = new HashMap<>();

    public static void addNeighborBlockEntityWeapons(BlockEntityType type, BiConsumer<TargetReceiverBlockEntity, BlockEntity> consumer) {
        neighborWeapons.put(type, consumer);
    }
    public static boolean validWeapon(BlockEntityType be) { return neighborWeapons.containsKey(be); }
    public static void removeWeapon(BlockEntityType be) { neighborWeapons.remove(be); }

    public TargetReceiverBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.targetReceiverBlockEntity.get(), pos, state);
        
        colorId = 0;
        targetColorId = 0;
        this.uiTargetBlockPos = null;
        this.playerFiredWeapon = null;
        this.linkedTargetController = null;
        this.manager = null;
    }

    private final int REFRESH_TICKS = 20;
    private int ticks = 0;
    public void tick(Level level, BlockPos pos, BlockState state, TargetReceiverBlockEntity blockEntity)
    {
        if (level.isClientSide) return;
        if(ticks++ < REFRESH_TICKS) return;
        ticks = 0;
        // Set up manager if not already set
        if (this.manager == null) {
            this.manager = SatelliteManager.get(level);
        }
        
        // Attempt to set linkedTargetController if we have a manager and valid color IDs
        if (this.manager != null && this.linkedTargetController == null) {
            this.linkedTargetController = this.manager.getTargetController(this.colorId, this.targetColorId);
            if(this.linkedTargetController != null) {
                this.updateBlockState(true);
            }

        }

        if(this.linkedTargetController != null) {
            setUiTargetBlockPos(this.linkedTargetController.getUiTargetBlockPos());
        }
    }

    public BlockPos getUiTargetBlockPos() {
        return uiTargetBlockPos;
    }

    public void setUiTargetBlockPos(BlockPos blockPos) {
        if(blockPos == uiTargetBlockPos) return;
        SatelliteEventManager.fireTargetReceiverTargetSet(this, this.linkedTargetController,
         this.uiTargetBlockPos, blockPos);
        this.uiTargetBlockPos = blockPos;
        if(this.linkedTargetController != null)
            this.linkedTargetController.addTargetReceiver(this);
        markUpdated();
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int color) {
        this.colorId = color;
        this.linkedTargetController = null; // Reset link when color changes
        this.updateBlockState(false);
    }

    public int getTargetColorId() {
        return targetColorId;
    }

    public void setTargetColorId(int colorId) {
        this.targetColorId = colorId;
        this.linkedTargetController = null; // Reset link when target color changes
        this.updateBlockState(false);
    }


    protected void updateBlockState(boolean isOn) {
        if(this.level == null) return;
        BlockState state = this.getBlockState();
        BlockState newState = state.setValue(SatelliteDisplayBlock.POWERED, isOn);

        //fire TargetReceiver linked or unlinked
        if(isOn) {
            SatelliteEventManager.fireTargetReceiverLinked(this, this.linkedTargetController);
        } else {
            SatelliteEventManager.fireTargetReceiverUnlinked(this, this.linkedTargetController );
        }

        level.setBlock(this.getBlockPos(), newState, 3);
        this.setChanged();
        level.sendBlockUpdated(this.getBlockPos(), state, newState, 3);
    }

    public Player getPlayerFiredWeapon() {
        return playerFiredWeapon;
    }

    public void use(Player p, InteractionHand hand, BlockHitResult hitResult)
    {
        if(this.level == null || level.isClientSide) return;
        
        // Handle target color setting similar to TargetControllerBlockEntity
        int cmd = ISatelliteControllerBE.calculateHitCommandTargetReceiver(hitResult);
        if(cmd == -1) return;

        if(cmd == 0) {
            setColorId(-1);
            setTargetColorId(-1);
            //turnOff
        } else {
            int blockId = -1;
            if( p.getItemInHand(hand).getItem() instanceof BlockItem bi ) {
                Block b = bi.getBlock();
                blockId = SatelliteManager.getColorId(b);
            }

            if(blockId != -1 ) {
                if(cmd == 1) setColorId(blockId);
                if(cmd == 2) setTargetColorId(blockId);
            } else {
                int total = SatelliteManager.totalIds();
                if(cmd == 1) setColorId( (getColorId() + 1) % total );
                if(cmd == 2) setTargetColorId( (getTargetColorId() + 1) % total );
            }
        }

    }

    public static final Vec3i[] NEIGHBOR_COORDS = {
        new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0),
        new Vec3i(0, 1, 0), new Vec3i(0, -1, 0),
        new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)
    };
    public void fireWeapon(Player p)
    {
        if(this.level == null || level.isClientSide) return;
        if(this.linkedTargetController == null) return;
        this.uiTargetBlockPos = this.linkedTargetController.getUiTargetBlockPos();
        this.playerFiredWeapon = p;
        
        //Check if any neigboring block entities are valid weapons
        for(Vec3i offset : NEIGHBOR_COORDS) {
            BlockPos neighborPos = this.getBlockPos().offset(offset);
            BlockEntity neighborBE = this.level.getBlockEntity(neighborPos);
            if(neighborBE != null && validWeapon(neighborBE.getType())) {
                var biconsumer = neighborWeapons.get(neighborBE.getType());
                biconsumer.accept(this, neighborBE);
            }
        }

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
