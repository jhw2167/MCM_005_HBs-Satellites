package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBlock;
import com.holybuckets.satellite.core.SatelliteDisplay;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;

import java.util.*;

import static com.holybuckets.satellite.SatelliteMain.chiselBitsApi;

public class SatelliteControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBlock
{
    int colorId;
    BlockPos satelliteTargetPos;
    SatelliteBlockEntity linkedSatellite;
    boolean forceDisplayUpdates;
    final Commands commands;

    private static final int PLAYER_RANGE = 64;

    private static class Commands {
        boolean hasUpdate;
        int dSection, dNS, dEW, dDepth, dIdAdj, dIdSet;
        BlockHitResult playerSelection;
        Commands() {
            hasUpdate = false;
            dSection = 0; dNS = 0; dEW = 0; dDepth = 0;
            dIdAdj = 0; dIdSet = 0;
            playerSelection = null;
        }

        public void reset() {
            dSection = 0; dNS = 0; dEW = 0; dDepth = 0;
            dIdAdj = 0; dIdSet = 0;
            playerSelection = null;
            this.hasUpdate = false;
        }
    }

    public SatelliteControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteControllerBlockEntity.get(), pos, state);
        this.setColorId(0);
        this.forceDisplayUpdates = false;
        this.linkedSatellite = null;
        this.satelliteTargetPos = null;

        commands = new Commands();
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

        this.forceUpdate();
        if(this.linkedSatellite != null) {
            this.satelliteTargetPos = this.linkedSatellite.getBlockPos();
        } else {
            this.satelliteTargetPos = null;
        }
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
            int dNS=0,dEW=0;
            switch (cmd) {
                case 1: dNS = -1; break;   //north
                case 2: dNS = 1; break;  //south
                case 3: dEW = -1; break;   //east
                case 4: dEW = 1; break;  //west
                default: dNS = 0; dEW = 0; break;
            }
            //this.source.adjOrdinal(dNS, dEW);
            commands.dNS += dNS;
            commands.dEW += dEW;

        } else if( cmd < 7) {   //adjust depth 5 - increase depth, 6 - decrease depth
            //this.source.adjCurrentSection( (cmd == 5 ? 1 : -1) );
            commands.dSection += (cmd == 5 ? 1 : -1);
        } else if ( cmd < 9) {   //adjust display height
            commands.dDepth += (cmd == 7 ? 1 : -1);
        } else if (cmd == 16){
            commands.dIdAdj += 1; //next wool color
        } else if (cmd >= 17) {
            commands.dIdSet = cmd - 16; //set wool color
        }

        commands.hasUpdate = true;
    }

    @Override
    public void forceUpdate() {
        this.ticks = PATH_REFRESH_TICKS-1; //force update next tick
        this.forceDisplayUpdates = true;
    }

    public void turnOff() {
        this.toggleOnOff(false);
        this.clearDisplay();
        if(this.source != null) {
            this.source.clear();
            this.source.resetChunkSection();
            this.source.resetOrdinal();
        }
        this.forceUpdate();
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
        //1. Recover Satellite if lost between chunk loads
        if(this.linkedSatellite == null && this.satelliteTargetPos != null) {
            recoverSatellite();
        }

        if(this.linkedSatellite != SatelliteManager.get(this.colorId)) {
            this.linkedSatellite = SatelliteManager.get(this.colorId);
            setSatellite(this.linkedSatellite);
        }


        if(this.source != null && this.isDisplayOn ) {
            processCommands();
            renderDisplay();
            renderPlayerUI();
        }

    }

    private static final int UI_REFRESH_TICKS = 10;
    private void processCommands()
    {
        if(ticks % UI_REFRESH_TICKS != 0) return;
        if(!this.commands.hasUpdate) return;

        if(this.commands.dIdSet > 0) {
            this.setColorId(this.commands.dIdSet);
            this.commands.dIdSet = 0;
        } else if(this.commands.dIdAdj != 0) {
            int newId = (this.getColorId() + this.commands.dIdAdj) % 16;
            if(newId < 0) newId += 16;
            this.setColorId(newId);
            this.commands.dIdAdj = 0;
        }

        if(this.source == null || this.source.noSource() || !this.isDisplayOn) {
            this.commands.reset();
            return;
        }

        this.source.adjCurrentSection(this.commands.dSection);
        this.commands.dSection = 0;

        this.source.adjOrdinal(this.commands.dNS, this.commands.dEW);
        this.commands.dNS = 0;
        this.commands.dEW = 0;

        this.source.adjDisplayDepth(this.commands.dDepth);
        this.commands.dDepth = 0;

        this.commands.hasUpdate = false;
        this.forceUpdate();
    }

    private static final int PATH_REFRESH_TICKS = 200;
    private static final int ENTITY_REFRESH_TICKS = 5;
    private void renderDisplay() {
        if(ticks % PATH_REFRESH_TICKS == 0 ){
            propagateToNeighbors();
            if(true || forceDisplayUpdates) {
                source.resetDisplayUpdates();
                this.forceDisplayUpdates = false;
            }
        }

        if(this.source != null && ticks % ENTITY_REFRESH_TICKS == 0)
            this.source.renderEntities(this.getBlockPos());
    }

    private static int PLAYER_UI_REFRESH_TICKS = 4;
    private static int REACH_DIST_BLOCKS = 3;
    private void renderPlayerUI()
    {
        //Get all players within 64 blocks
        if(ticks % PLAYER_UI_REFRESH_TICKS != 0) return;

        List<ServerPlayer> players = HBUtil.PlayerUtil
            .getAllPlayersInBlockRange(getBlockPos(), PLAYER_RANGE );
        for(ServerPlayer player : players) {
            HitResult res = player.pick(REACH_DIST_BLOCKS, 0.5f, true);
            if( !source.isHitWithinDisplay(res.getLocation()) ) continue;
            BlockHitResult bhr = (BlockHitResult) res;
            if( !chiselBitsApi.isViewingHoloBlock(player.level(), bhr)) continue;
            this.source.renderUI( player, bhr );
        }
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
            temp.setSource(source, forceDisplayUpdates);
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

        this.source.collectEntities();
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
