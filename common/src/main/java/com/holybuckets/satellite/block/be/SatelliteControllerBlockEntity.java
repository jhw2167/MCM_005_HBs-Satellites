package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.satellite.block.SatelliteControllerBlock;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.client.core.SatelliteDisplayClient;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;

import java.util.*;

import static com.holybuckets.satellite.SatelliteMain.chiselBitsApi;

public class SatelliteControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE
{
    int colorId;
    SatelliteManager manager;
    BlockPos uiTargetBlockPos;
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
            dIdAdj = 0; dIdSet = -1;
            playerSelection = null;
        }

        public void reset() {
            dSection = 0; dNS = 0; dEW = 0; dDepth = 0;
            dIdAdj = 0; dIdSet = -1;
            playerSelection = null;
            this.hasUpdate = false;
        }
    }

    public SatelliteControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.satelliteControllerBlockEntity.get(), pos, state);
        this.setColorId(0);
        this.uiTargetBlockPos = BlockPos.ZERO;
        this.forceDisplayUpdates = false;
        this.linkedSatellite = null;
        this.satelliteTargetPos = null;

        commands = new Commands();
    }


    @Override
    public BlockPos getUiPosition() {
        return uiTargetBlockPos;
    }

    //setTargetPosition, setSelectedPosition
    public void setUiPosition(BlockPos blockTarget) {
        this.uiTargetBlockPos = blockTarget;
        markUpdated();
        if(level == null || blockTarget == null) return;

        //message local players
        List<ServerPlayer> players = HBUtil.PlayerUtil
            .getAllPlayersInBlockRange(getBlockPos(), PLAYER_RANGE );
        for(ServerPlayer player : players) {
            Messager.getInstance().sendChat(player,
            "Targeted: " + HBUtil.BlockUtil.positionToString(blockTarget));
        }
    }

    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public void setColorId(int colorId) {
        this.colorId = colorId;
        this.markUpdated();
    }


    @Override
    public void setSource(SatelliteDisplay source, boolean forceDisplayUpdates)
    {
        this.source = source;
        satelliteTargetPos = (linkedSatellite != null) ? linkedSatellite.getBlockPos() : null;
        if(forceDisplayUpdates) forceUpdate();
        if(source == null || source.noSource()) return;
        this.displayInfo = source.initDisplayInfo(this);
    }

    public void onDestroyed() {
        this.turnOff();
    }

    private void turnOff() {
        this.clearDisplay();
        if(this.source != null) {
            this.source.clear();
            this.source.resetChunkSection();
            this.source.resetOrdinal();
        }
        this.forceUpdate();
    }

    public void use(Player p, InteractionHand hand, BlockHitResult res)
    {
        int cmd = ISatelliteControllerBE.calculateHitCommand(res);
        if(cmd == -1) return;
        this.commands.hasUpdate = true;

        if(cmd == 0) {
            this.toggleOnOff(!this.isDisplayOn);
        }


        if (cmd == 16)
        {
            //If player is holding wool in their hand, set to that color
            if( p.getItemInHand(hand).getItem() instanceof BlockItem bi ) {
                Block b = bi.getBlock();
                commands.dIdSet = manager.getColorId(b);
            }

            if(commands.dIdSet < 0) {
                commands.dIdAdj += 1; //next wool color
            }

        } else if (cmd >= 17) {
            commands.dIdSet = cmd - 17; //set wool color
        }

        if(!this.isDisplayOn || this.source == null) {
            return;
        }

        if(cmd == 0) {
            //handled above
        } else if( cmd < 5) {   //adjust ordinally
            int dNS=0,dEW=0;
            switch (cmd) {
                case 1: dNS = 1; break;   //north
                case 2: dNS = -1; break;  //south
                case 3: dEW = 1; break;   //east
                case 4: dEW = -1; break;  //west
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
        }

    }

    @Override
    public void forceUpdate() {
        this.ticks = PATH_REFRESH_TICKS-1; //force update next tick
        this.forceDisplayUpdates = true;
    }

    @Override
    public void toggleOnOff(boolean toggle)
    {
        if(isDisplayOn == toggle) return;
        isDisplayOn = toggle;
        if(!isDisplayOn) turnOff();
        this.markUpdated();
        this.updateBlockState();
    }


    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.satelliteControllerBlockEntity.get();
    }


    public static int UI_REFRESH_TICKS = 10;
    private void processCommands()
    {
        if(ticks % UI_REFRESH_TICKS != 0) return;
        if(!this.commands.hasUpdate) return;

        if(this.commands.dIdSet > -1) {
            this.setColorId(this.commands.dIdSet);
            this.commands.dIdSet = -1;
        } else if(this.commands.dIdAdj != 0) {
            int newId = (this.getColorId() + this.commands.dIdAdj) % SatelliteManager.totalIds();
            this.setColorId(newId);
            this.commands.dIdAdj = 0;
        }

        if(!this.isDisplayOn || this.source == null || this.source.noSource()) {
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

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteBlockEntity)
    {
        super.tick(level, blockPos, blockState, satelliteBlockEntity);
        if (this.level.isClientSide) {
            if(this.isDisplayOn) {
                if(source == null) this.source = new SatelliteDisplayClient(level, this);
                if(ticks % PATH_REFRESH_TICKS == 0) { propagateToNeighbors(); }
            } else {
                if(source != null) this.source.clear();
                this.source = null;
            }
            return;
        }
        if(manager == null) manager = SatelliteManager.get(level);

        //1. Recover Satellite if lost between chunk loads
        if(this.linkedSatellite == null && this.satelliteTargetPos != null) {
            recoverSatellite();
        }

        if(this.linkedSatellite != manager.get(this.colorId)) {
            this.linkedSatellite = manager.get(this.colorId);
            SatelliteDisplay source = manager.generateSource(this.linkedSatellite, this);
            setSource(source, true);
        }


        processCommands();
        renderDisplay();

    }

    public static int PATH_REFRESH_TICKS = 200;
    public static int ENTITY_REFRESH_TICKS = 10;
    protected void renderDisplay()
    {

        if( (isDisplayOn && ticks%PATH_REFRESH_TICKS==0) )
        {
            propagateToNeighbors();
            if(forceDisplayUpdates) {
                if(source != null) source.resetDisplayUpdates();
                this.forceDisplayUpdates = false;
            } else {
                if(source != null) source.setNeedsUpdate(false);
            }
        }
        super.renderDisplay();

        if(this.source != null && ticks % ENTITY_REFRESH_TICKS == 0)
            this.source.renderEntities(this.getBlockPos());

        this.renderPlayerUI();
    }

    private static int PLAYER_UI_REFRESH_TICKS = 4;
    public static double REACH_DIST_BLOCKS = 0.583f*3;
    private void renderPlayerUI()
    {
        //Get all players within 64 blocks
        if( !isDisplayOn || source==null || source.noSource() ) return;
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
        LevelChunk distantChunk = manager.getChunk(level, this.satelliteTargetPos);
        if(distantChunk != null) {
            BlockEntity be = distantChunk.getBlockEntity(this.satelliteTargetPos);
            if(be instanceof SatelliteBlockEntity) {
                manager.put(this.colorId, (SatelliteBlockEntity) be);
            } else {
                this.satelliteTargetPos = null;
            }
        }
    }

    public void propagateToNeighbors()
    {
        if (source == null || level == null) return;

        // Find all connected display blocks via flood fill
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, ISatelliteDisplayBE> nodes = new HashMap<>();
        Queue<BlockPos> toCheck = new LinkedList<>();

        // Start from controller position
        toCheck.offer(getBlockPos());
        visited.add(getBlockPos());

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            ISatelliteDisplayBE temp = (ISatelliteDisplayBE) level.getBlockEntity(current);
            if(!this.level.isClientSide) temp.setSource(source, forceDisplayUpdates);
            nodes.put(current, temp);

            // Check all horizontal neighbors
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);

                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                BlockEntity be = level.getBlockEntity(neighbor);
                if (be instanceof ISatelliteDisplayBE displayBE) {
                    toCheck.offer(neighbor);
                }
            }
        }

        source.clear();
        source.addAll(nodes);

        this.source.collectEntities();
    }

    //** Serialization

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        tag.putBoolean("isDisplayOn", isDisplayOn);
        if(uiTargetBlockPos != null) {  //saved to send to client for rendering
            String pos = HBUtil.BlockUtil.positionToString(uiTargetBlockPos);
            tag.putString("uiTargetBlockPos", pos);
        }
        if(satelliteTargetPos != null) {
            String pos = HBUtil.BlockUtil.positionToString(satelliteTargetPos);
            tag.putString("satelliteTargetPos", pos);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
        isDisplayOn = tag.getBoolean("isDisplayOn");

        if(tag.contains("uiTargetBlockPos")) {
            String targetPosStr = tag.getString("uiTargetBlockPos");
            uiTargetBlockPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(targetPosStr) );
        }
        if(tag.contains("satelliteTargetPos")) {
            String pos = tag.getString("satelliteTargetPos");
            satelliteTargetPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(pos) );
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

    private void updateBlockState() {
        if(this.level == null) return;
        BlockState state = this.getBlockState();
        BlockState newState = state.setValue(SatelliteControllerBlock.POWERED, this.isDisplayOn);
        level.setBlock(this.getBlockPos(), newState, 3);
    }

}
