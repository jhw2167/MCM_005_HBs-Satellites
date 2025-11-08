package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.CommonClass;
import com.holybuckets.satellite.CommonProxy;
import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteBE;
import com.holybuckets.satellite.core.SatelliteManager;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ChunkPos;
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
    SatelliteManager manager;
    LevelChunk currentChunk;

    BlockPos targetPos;     //Satellite target position that it is traveling to
    BlockPos travelPos;
    boolean traveling;
    private int ticks;

    private static final int HEXCODE_MAX = 0xFFFFFF;

    public SatelliteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteBlockEntity.get(), pos, state);
        //this.setColorId( (int) (Math.random() * HEXCODE_MAX));
        this.colorId = -1;
        this.targetPos = pos;
        traveling = false;
    }

    public void use(Player p, InteractionHand hand, BlockHitResult res) {
        if (!hand.equals(InteractionHand.MAIN_HAND)) return;
        //if top face clicked
        boolean isClientSide = this.level.isClientSide;
        boolean isTopFaceUsed = res.getDirection().equals(net.minecraft.core.Direction.UP);
        if(isClientSide && isTopFaceUsed) {
            CommonClass.clientSideActions(this.level, this);
        }


        if(isTopFaceUsed) return;


        int cmd = 16;
        if (cmd == 16)
        {
            //If player is holding wool in their hand, set tot that color
            int color = (colorId + 1) % SatelliteManager.totalIds();
            if( p.getItemInHand(hand).getItem() instanceof BlockItem bi ) {
                Block b = bi.getBlock();
                color = manager.getColorId(b);
            }
            setColorId(color);
        }
    }

    @Override
    public int getColorId() {
        return colorId;
    }

    /**
     * Forcibly remove any satellite associated with the old colorId from the manager, then set to new colorId
     * @param colorId
     */
    @Override
    public void setColorId(int colorId) {
        if(colorId < 0 || colorId >= SatelliteManager.totalIds()) return;
        manager.remove(this.colorId, this);
        this.colorId = colorId;
        this.markUpdated();
    }

    public void setLevelChunk(LevelChunk chunk) {
        this.currentChunk = chunk;
    }

    @Override
    public void setTargetPos(BlockPos blockPos) {
        int y = Math.max(blockPos.getY(), SatelliteMain.CONFIG.satelliteConfig.minSatelliteWorkingHeight);
        this.targetPos = blockPos.atY(y);
    }

    @Override
    public void launch(BlockPos pos) {
        if(pos != null) setTargetPos(pos);
        this.traveling = true;
        this.ticks = (TICKS_PER_MINUTE-1);
    }

    @Override
    public BlockPos getTargetPos() {
        return targetPos;
    }

    @Override
    public boolean isTraveling() { return traveling; }


    public void onDestroyed() {
        manager.remove(this.colorId, this);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.satelliteBlockEntity.get();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteBlockEntity satelliteBlockEntity)
    {
        ticks++;
        if(manager == null) manager = manager.get(level);
        if (this.level.isClientSide) return;
        manager.put(this.colorId, this);

        if(traveling) this.travel();

    }

    private static int TICKS_PER_MINUTE = 20;//.20*60;
    private void travel()
    {
        if(ticks%TICKS_PER_MINUTE!=0) return;

        if(getBlockPos().equals(targetPos) || level == null) {
            traveling = false;
            return;
        }

        BlockPos currentPos = (travelPos != null) ? travelPos : this.getBlockPos();
        ChunkPos currentChunk = new ChunkPos(currentPos);
        ChunkPos targetChunk = new ChunkPos(targetPos);
        if(currentChunk.equals(targetChunk)) {
            if( manager.moveSatellite(this, targetPos) ) {
                traveling = false;
                travelPos = null;
            }
            return;
        }

        int chunksToMove = SatelliteMain.CONFIG.satelliteConfig.satelliteTravelRateChunksPerSecond*60;
        int dx = targetChunk.x - currentChunk.x;
        int dz = targetChunk.z - currentChunk.z;

        //move n chunks towards target x and z where n <= chunksToMove
        int moveX=0, moveZ=0;
        if(targetChunk.x > currentChunk.x) {
            moveX = Math.min(chunksToMove, dx);
        } else if (targetChunk.x < currentChunk.x) {
            moveX = -Math.min(chunksToMove, -dx);
        }

        if(targetChunk.z > currentChunk.z) {
            moveZ = Math.min(chunksToMove, dz);
        } else if (targetChunk.z < currentChunk.z) {
            moveZ = -Math.min(chunksToMove, -dz);
        }

        //Move block entity to new position
        BlockPos newPos = currentPos.offset(moveX * 16, 0, moveZ * 16);
        if(moveX < chunksToMove && moveZ < chunksToMove) {
            int yPos = SatelliteMain.CONFIG.satelliteConfig.minSatelliteWorkingHeight;
           newPos = targetChunk.getWorldPosition().atY(yPos);
        }
        travelPos = newPos;
        /*
        TOO ADVANCED FOR NOW
        BlockState state = level.getBlockState(currentPos);
        level.setBlockAndUpdate(newPos, state);
        level.removeBlock(currentPos, false);
        BlockEntity be = level.getBlockEntity(newPos);
        if(be instanceof SatelliteBlockEntity satBE) {
            satBE.targetPos = this.targetPos;
            satBE.traveling = this.traveling;
            manager.remove(this.colorId);
            satBE.setColorId( this.colorId );
            satBE.manager = this.manager;
            satBE.markUpdated();
        }
        */

    }

    // Package-private subclass that implements ISatelliteBE for screen usage
    static class ScreenSatelliteWrapper implements ISatelliteBE {
        private final SatelliteBlockEntity satellite;
        
        ScreenSatelliteWrapper(SatelliteBlockEntity satellite) {
            this.satellite = satellite;
        }
        
        @Override
        public int getColorId() {
            return satellite.getColorId();
        }
        
        @Override
        public void setColorId(int colorId) {
            satellite.setColorId(colorId);
        }
        
        @Override
        public BlockPos getTargetPos() {
            return satellite.getTargetPos();
        }
        
        @Override
        public void setTargetPos(BlockPos targetPos) {
            satellite.setTargetPos(targetPos);
        }
        
        @Override
        public BlockPos getBlockPos() {
            return satellite.getBlockPos();
        }
        
        @Override
        public boolean isTraveling() {
            return satellite.isTraveling();
        }
        
        @Override
        public void launch(BlockPos targetPos) {
            satellite.launch(targetPos);
        }
        
        @Override
        public Level getLevel() {
            return satellite.getLevel();
        }
    }
    
    // Method to create wrapper for screen usage
    public ISatelliteBE createScreenWrapper() {
        return new ScreenSatelliteWrapper(this);
    }



    //** Serialization

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        tag.putBoolean("traveling", traveling);
        if(traveling && travelPos != null) {
            String pos = HBUtil.BlockUtil.positionToString(travelPos);
            tag.putString("travelPos", pos);
        }
        if(traveling && targetPos != null) {
            String pos = HBUtil.BlockUtil.positionToString(targetPos);
            tag.putString("targetPos", pos);
        }

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
        traveling = tag.getBoolean("traveling");
        if(tag.contains("travelPos")) {
            String pos = tag.getString("travelPos");
            this.travelPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(pos) );
        }
        if(tag.contains("targetPos")) {
            String pos = tag.getString("targetPos");
            this.targetPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(pos) );
        }
        if(getBlockPos().equals(targetPos) || travelPos == null || travelPos.equals(targetPos)) {
            traveling = false;
            targetPos = getBlockPos();
            travelPos = null;
        }
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
