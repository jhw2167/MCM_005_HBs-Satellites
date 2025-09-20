package com.holybuckets.satellite.block.be;

import com.holybuckets.satellite.SatelliteMain;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.ChunkDisplayInfo;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteDisplayUpdate;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class SatelliteControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBlock
{
    int colorId;
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

    @Override
    public void setRemoved() {
        if(this.source != null) this.source.clear();
        super.setRemoved();
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
        if (this.level.isClientSide) return;
        if(this.linkedSatellite != SatelliteManager.get(this.colorId)) {
            this.linkedSatellite = SatelliteManager.get(this.colorId);
            if(this.source != null) this.source.clear();
            this.source = new SatelliteDisplay(level, this.linkedSatellite, this);
            this.source.add(this.getBlockPos(), this);

            propagateToNeighbors();
        }

        if(ticks++ >= PATH_REFRESH_TICKS) {
            ticks = 0;
            if(this.source != null) {
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
    }




}
