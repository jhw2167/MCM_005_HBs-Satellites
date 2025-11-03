package com.holybuckets.satellite.block;

import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class SatelliteBlock extends Block implements EntityBlock {
    
    public SatelliteBlock() {
        super(Properties.of()
            .lightLevel(state -> 12)
            .noOcclusion() 
            .isViewBlocking((state, level, pos) -> false)
            .destroyTime(0.6f)  // Makes it break faster
            .explosionResistance(6f));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteBlockEntity(pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player p) {
        super.playerWillDestroy(level, pos, state, p);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SatelliteBlockEntity) {
            SatelliteBlockEntity satelliteBE = (SatelliteBlockEntity) be;
            satelliteBE.onDestroyed();
        }
    }

    public InteractionResult use(BlockState $$0, Level $$1, BlockPos $$2, Player p, InteractionHand hand, BlockHitResult hitResult) {

        BlockEntity be = $$1.getBlockEntity($$2);
        if (be instanceof SatelliteBlockEntity sat) {
            sat.use(p, hand, hitResult);
            //return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }


    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (l, pos, s, blockEntity) -> ((SatelliteBlockEntity) blockEntity).tick(l, pos, state, (SatelliteBlockEntity) blockEntity);
    }
}
