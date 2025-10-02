package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

public class SatelliteControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBlock
{
    int colorId;
    BlockPos satelliteTargetPos;
    SatelliteDisplay source;
    SatelliteBlockEntity linkedSatellite;

    public SatelliteControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteControllerBlockEntity.get(), pos, state);
        this.setColorId(0);
    }


    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    @Override
    public void setSatellite(SatelliteBlockEntity satellite) {

        if(this.source != null) this.source.clear();
        this.source = new SatelliteDisplay(level, this.linkedSatellite, this);
        this.source.add(this.getBlockPos(), this);
        this.source.setDepth(2);

        propagateToNeighbors();
        if(this.linkedSatellite != null) {
            this.satelliteTargetPos = this.linkedSatellite.getBlockPos();
        } else {
            this.satelliteTargetPos = null;
        }
        if(HBUtil.PlayerUtil.getAllPlayers().isEmpty()) return;
        Player p = HBUtil.PlayerUtil.getAllPlayers().get(0);
        this.source.addEntity(p);
    }

    public void onDestroyed() {
        this.turnOff();
    }

    public void use(BlockHitResult res)
    {
        int cmd = -1;
        cmd = ISatelliteControllerBlock.calculateHitCommand(res);

        if(cmd == 0) this.toggleOnOff(!this.isDisplayOn);

        if(!this.isDisplayOn || this.source == null) {
            this.turnOff();
            return;
        }

        if(cmd == 0) {
            //handled above
        } else if( cmd < 5) {   //adjust ordinally
            //not implemented
        } else if( cmd < 7) {   //adjust depth 5 - increase depth, 6 - decrease depth
            //this.source.adjDepth( cmd == 5 ? 1 :-1 );
            this.source.adjCurrentSection( cmd == 5 ? -1 : 1 );
        }

        this.ticks = PATH_REFRESH_TICKS; //force update next tick
    }

    public void turnOff() {
        this.toggleOnOff(false);
        this.clearDisplay();
        if(this.source != null) {
            this.source.clear();
            this.source.setDepth(1);
        }
    }


    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.satelliteControllerBlockEntity.get();
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteBlockEntity)
    {
        super.tick(level, blockPos, blockState, satelliteBlockEntity);
        if (this.level.isClientSide) return;
        //1. Recover Satellite if lost
        if(this.linkedSatellite == null && this.satelliteTargetPos != null) {
            recoverSatellite();
        }

        if(this.linkedSatellite != SatelliteManager.get(this.colorId)) {
            this.linkedSatellite = SatelliteManager.get(this.colorId);
            setSatellite(this.linkedSatellite);
        }


        if(this.source != null && this.isDisplayOn ) {
            renderDisplay();
        }

    }

    private static final int PATH_REFRESH_TICKS = 200;
    private static final int ENTITY_REFRESH_TICKS = 5;
    private int ticks = 0;
    private void renderDisplay() {
        if(ticks++ >= PATH_REFRESH_TICKS) {
            ticks = 0;
            propagateToNeighbors();
        }

        if(this.source != null && ticks % ENTITY_REFRESH_TICKS == 0)
            this.source.renderEntities(this.getBlockPos());
    }

    private void recoverSatellite()
    {
        if(this.satelliteTargetPos == null) return;
        LevelChunk distantChunk = SatelliteManager.getChunk(level, this.satelliteTargetPos);
        if(distantChunk != null) {
            BlockEntity be = distantChunk.getBlockEntity(this.satelliteTargetPos);
            if(be instanceof SatelliteBlockEntity) {
                SatelliteManager.put(this.colorId, (SatelliteBlockEntity) be);
            } else {
                this.satelliteTargetPos = null;
            }
        }
    }

    public void propagateToNeighbors()
    {
        if (source == null) return;
        this.source.collectEntities();

        Level level = getLevel();
        if (level == null || level.isClientSide()) return;

        // Find all connected display blocks via flood fill
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, ISatelliteDisplayBlock> nodes = new HashMap<>();
        Queue<BlockPos> toCheck = new LinkedList<>();

        // Start from controller position
        toCheck.offer(getBlockPos());
        visited.add(getBlockPos());

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            ISatelliteDisplayBlock temp = (ISatelliteDisplayBlock) level.getBlockEntity(current);
            temp.setSource(source);
            nodes.put(current, temp);

            // Check all horizontal neighbors
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);

                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                BlockEntity be = level.getBlockEntity(neighbor);
                if (be instanceof ISatelliteDisplayBlock displayBE) {
                    toCheck.offer(neighbor);
                }
            }
        }

        source.clear();
        source.addAll(nodes);

    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        if(satelliteTargetPos != null) {
            String pos = HBUtil.BlockUtil.positionToString(satelliteTargetPos);
            tag.putString("satelliteTargetPos", pos);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
        if(tag.contains("satelliteTargetPos")) {
            String pos = tag.getString("satelliteTargetPos");
            satelliteTargetPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(pos) );
        }
    }


}
