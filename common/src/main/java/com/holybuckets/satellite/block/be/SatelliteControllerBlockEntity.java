package com.holybuckets.satellite.block.be;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.CommonClass;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteControllerBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteDisplayBE;
import com.holybuckets.satellite.block.be.isatelliteblocks.ITargetController;
import com.holybuckets.satellite.client.core.SatelliteDisplayClient;
import com.holybuckets.satellite.core.SatelliteDisplay;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.satellite.core.SatelliteWeaponManager;
import com.holybuckets.satellite.item.SatelliteItemUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SatelliteControllerBlockEntity extends SatelliteDisplayBlockEntity implements ISatelliteControllerBE, ITargetController
{
    int colorId;
    SatelliteManager manager;
    BlockPos uiTargetBlockPos;
    Vec3 uiCursorPos;
    BlockPos satelliteTargetPos;
    BlockPos satelliteTravelPos;
    SatelliteBlockEntity linkedSatellite;
    boolean forceDisplayUpdates;
    String satelliteDisplayError;
    final Commands commands;

    public static int PLAYER_RANGE = 64;

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
        this.uiCursorPos = Vec3.ZERO;
        this.forceDisplayUpdates = false;
        this.linkedSatellite = null;
        this.satelliteTargetPos = null;
        this.satelliteTravelPos = null;
        this.satelliteDisplayError = "";

        commands = new Commands();
    }


    @Override
    public SatelliteControllerBlockEntity getSatelliteController() {
        return this;
    }

    @Override
    public BlockPos getUiTargetBlockPos() {
        return uiTargetBlockPos;
    }


    //setTargetPosition, setSelectedPosition
    public void setUiTargetBlockPos(BlockPos blockTarget)
    {
        this.uiTargetBlockPos = blockTarget;
        markUpdated();
        if(level == null || blockTarget == null) return;

        //message local players
        List<ServerPlayer> players = HBUtil.PlayerUtil
            .getAllPlayersInBlockRange(getBlockPos(), PLAYER_RANGE );
        for(ServerPlayer player : players) {
            CommonClass.MESSAGER.sendBottomActionHint(player,
                "Targeted: " + HBUtil.BlockUtil.positionToString(blockTarget));
        }
    }

    public void setError(String error) {
        this.satelliteDisplayError = error;
        this.markUpdated();
    }

    @Override
    public int getColorId() {
        return colorId;
    }

    @Override
    public int getTargetColorId() {
        return colorId;
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
    }


    @Override
    public void setColorId(int colorId) {
        SatelliteWeaponManager.clearWaypoints(this);
        this.colorId = colorId;
        this.markUpdated();
    }

    public SatelliteBlockEntity getLinkedSatellite() {
        return this.linkedSatellite;
    }

    public BlockPos getSatelliteTargetPos() {
        return this.satelliteTargetPos;
    }

    public SatelliteItemUpgrade[] getUpgrades() {
        if(this.source == null) return null;
        return source.getUpgrades();
    }

    @Override
    public void setSource(SatelliteDisplay source, boolean forceDisplayUpdates)
    {
        this.source = source;
        satelliteTargetPos = (linkedSatellite != null) ? linkedSatellite.getBlockPos() : null;
        if(forceDisplayUpdates) forceUpdate();
        if(source == null || source.noSource()) return;
        toggleOnOff(true);
        this.displayInfo = source.initDisplayInfo(this);
    }

    public void onDestroyed() {
        this.turnOff();
    }

    @Override
    public void clearDisplay() {
        super.clearDisplay();
        setCursorPosition(null);
        setUiTargetBlockPos(null);
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
        processInput(p, hand, cmd, null);
    }

    public void processInput(Player p, InteractionHand hand, int cmd, ISatelliteControllerBE controller)
    {
        if(cmd == -1) return;
        this.commands.hasUpdate = true;

        if(cmd == 0) {
            this.toggleOnOff(!this.isDisplayOn);
        }


        if (cmd == 16)
        {
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
        } else if ( cmd < 9) {   //adjust display height 7 - inc, 8 - dec
            commands.dDepth += (cmd == 7 ? 1 : -1);
        } else if( cmd == 9) {  //select Satellite block
            if(this.level.isClientSide)
                CommonClass.clientSideActions(this.level, this.clientCloneLinkedSatellite());
            else
                this.updateBlockState();    //force sync to client
            this.commands.hasUpdate = false;
        } else if ( cmd < 12 )
        {
            if(controller == null) return;
            ITargetController tc = (ITargetController) controller;
            if(cmd == 10) {
                if(tc.getCursorPosition() == null) {
                    source.setTargetController(tc);
                } else {
                    tc.setCursorPosition(null);
                    tc.setUiTargetBlockPos(null);
                    source.setTargetController(this);
                }
            } else if (cmd == 11) {
                if(tc.getUiTargetBlockPos() == null) {
                    CommonClass.MESSAGER.sendBottomActionHint(p, "No target selected!");
                }
                source.fire(p, (ITargetController) controller);
            }
            this.commands.hasUpdate = false;
        } else if( cmd < 16 ) //12-15, upgrade slots
        {
        /* HANDLED IN UPGRADECONTROLLERBLOCK

            SatelliteItemUpgrade prevItem = null;
             if(p.getItemInHand(hand).getItem() instanceof SatelliteItemUpgrade item) {
                 prevItem = this.source.addUpgrade(item, cmd-12);
                 //remove one from player hand
                 p.getItemInHand(hand).shrink(1);
             } else if(p.getItemInHand(hand).isEmpty() ) {
                 prevItem = this.source.removeUpgrade(cmd-12);
             }

            if(prevItem != null) {
                if (!p.getInventory().add(prevItem.getDefaultInstance())) {
                    p.drop(prevItem.getDefaultInstance(), false);
                }
            }
            */

        }

    }

    private ISatelliteBE clientCloneLinkedSatellite()
    {
        if(this.level == null || !this.level.isClientSide) return null;
        if(this.satelliteTravelPos == null) return null;

        ISatelliteBE clone = new SatelliteBlockEntity.ScreenSatelliteWrapper(
            this.getColorId(),
            this.satelliteTravelPos,
            this.getSatelliteTargetPos(),
            this.satelliteTravelPos != null,
            this.level
        );
        return clone;
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
        if(isDisplayOn)
            { if(source != null) source.resetChunkSection(); }
        else
            { turnOff(); }
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

    private static final int START_BUFFER = 40;
    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, SatelliteDisplayBlockEntity satelliteBlockEntity)
    {
        if(SatelliteManager.bufferSatelliteStart()) return;
        ticks++;
        if(manager == null) manager = SatelliteManager.get(level);

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


        if(ticks < START_BUFFER) { return; }
        else if(ticks == START_BUFFER) { this.propagateToNeighbors(); this.turnOff(); }
        else {}

        //1. Recover Satellite if lost between chunk loads
        if(this.linkedSatellite == null && this.satelliteTargetPos != null) {
            recoverSatellite();
        }

        if(this.linkedSatellite != manager.get(this.colorId))
        {
            linkedSatellite = manager.get(this.colorId);
            if(linkedSatellite == null) {
                toggleOnOff(false);
                setSource(source, true);
                propagateToNeighbors();
                return;
            }
            this.toggleOnOff(true);
            SatelliteDisplay source = manager.generateSource(this.linkedSatellite, this);
            setSource(source, true);
        }

        processCommands();
        renderDisplay();
        if(source!=null && isDisplayOn) source.resetRateLimiter();
    }

    public static int PATH_REFRESH_TICKS = 200;
    public static int ENTITY_REFRESH_TICKS = 10;
    protected void renderDisplay()
    {

        if( (isDisplayOn && ticks%PATH_REFRESH_TICKS==0) )
        {
            boolean playersInRange = HBUtil.PlayerUtil.getAllPlayersInBlockRange(getBlockPos(), PLAYER_RANGE ).size() > 0;
            if(!playersInRange) { this.turnOff(); return; }

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
        this.source.renderUI();

        /*
            List<ServerPlayer> players = HBUtil.PlayerUtil
                .getAllPlayersInBlockRange(getBlockPos(), PLAYER_RANGE );
            for(ServerPlayer player : players) {
                this.source.renderUI( null, player.pick(REACH_DIST_BLOCKS, 0.5f, true) );
            }
        */
    }

    private void recoverSatellite()
    {
        if(this.satelliteTargetPos == null) return;
        LoggerProject.logInfo("010100", HBUtil.BlockUtil.positionToString(this.getBlockPos()));
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

    //pathToNeighbors
    public void propagateToNeighbors()
    {
        if (level == null) return;

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

        if(source == null) return;
        source.clear();
        source.addAll(nodes);
        source.syncUpgradesWithControllers();

        source.setNeedsEntityUpdate(true);
        source.collectEntities();
        source.updateNeighbors();
    }

    //** Serialization

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("colorId", colorId);
        tag.putString("satelliteDisplayError", satelliteDisplayError);
        if(uiTargetBlockPos != null) {  //saved to send to client for rendering
            String pos = HBUtil.BlockUtil.positionToString(uiTargetBlockPos);
            tag.putString("uiTargetBlockPos", pos);
        }
        if(satelliteTargetPos != null) {
            String pos = HBUtil.BlockUtil.positionToString(satelliteTargetPos);
            tag.putString("satelliteTargetPos", pos);
        }
        if(linkedSatellite != null) {
            String pos = HBUtil.BlockUtil.positionToString(linkedSatellite.getTargetPos());
            tag.putString("satelliteTravelPos", pos);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        colorId = tag.getInt("colorId");
        satelliteDisplayError = tag.getString("satelliteDisplayError");

        if(tag.contains("uiTargetBlockPos")) {
            String targetPosStr = tag.getString("uiTargetBlockPos");
            uiTargetBlockPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(targetPosStr) );
        }
        if(tag.contains("satelliteTargetPos")) {
            String pos = tag.getString("satelliteTargetPos");
            satelliteTargetPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(pos) );
        }
        if(tag.contains("satelliteTravelPos")) {
            String pos = tag.getString("satelliteTravelPos");
            satelliteTravelPos = new BlockPos( HBUtil.BlockUtil.stringToBlockPos(pos) );
        } else {
            satelliteTravelPos = null;
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
