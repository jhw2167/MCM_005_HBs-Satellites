package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
        this.linkedSatellite = satellite;
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
            this.source.setDepth( cmd == 5 ? 1 :-1 );
        }


    }

    public void turnOff() {
        this.toggleOnOff(false);
        this.clearDisplay();
        if(this.source != null) this.source.clear();
    }


    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.satelliteControllerBlockEntity.get();
    }

    private static final int PATH_REFRESH_TICKS = 100;
    private int ticks = 0;
    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteBlockEntity)
    {
        super.tick(level, blockPos, blockState, satelliteBlockEntity);
        if (this.level.isClientSide) return;
        if(this.linkedSatellite == null && this.satelliteTargetPos != null) {
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
        if(this.linkedSatellite != SatelliteManager.get(this.colorId)) {
            this.linkedSatellite = SatelliteManager.get(this.colorId);
            if(this.source != null) this.source.clear();
            this.source = new SatelliteDisplay(level, this.linkedSatellite, this);
            this.source.add(this.getBlockPos(), this);

            propagateToNeighbors();
            if(this.linkedSatellite != null) {
                this.satelliteTargetPos = this.linkedSatellite.getBlockPos();
            } else {
                this.satelliteTargetPos = null;
            }
        }

        if(ticks++ >= PATH_REFRESH_TICKS) {
            ticks = 0;
            if(this.source != null && this.isDisplayOn ) {
                propagateToNeighbors();
            }
        }

    }

    public void propagateToNeighbors()
    {
        if (source == null) return;

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

    /**
     * This can adjust the colorId of the display
     * or adjust the chunk direction in any ordinal direction or height
     *  - 0-4 = N/S/E/W
     *  - 5,6 = up/down
     *  - 7-16 = reserved commands
     *  - all other numbers, set colorId
     * @param update
     */
    @Override
    public void updateServer(SatelliteDisplayUpdate update) {

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
