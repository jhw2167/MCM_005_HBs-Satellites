package com.holybuckets.satellite.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SatelliteEventManager {
    
    private static final List<Consumer<TargetReceiverLinkedEvent>> targetReceiverLinkedListeners = new ArrayList<>();
    private static final List<Consumer<TargetReceiverUnlinkedEvent>> targetReceiverUnlinkedListeners = new ArrayList<>();
    private static final List<Consumer<TargetReceiverTargetSetEvent>> targetReceiverTargetSetListeners = new ArrayList<>();
    private static final List<Consumer<TargetReceiverFireEvent>> targetReceiverFireListeners = new ArrayList<>();
    
    // Event classes
    public static class TargetReceiverLinkedEvent {
        private final BlockEntity receiver;
        private final BlockEntity targetController;
        
        public TargetReceiverLinkedEvent(BlockEntity receiver, BlockEntity targetController) {
            this.receiver = receiver;
            this.targetController = targetController;
        }
        
        public BlockEntity getReceiver() {
            return receiver;
        }
        
        public BlockEntity getTargetController() {
            return targetController;
        }
    }
    
    public static class TargetReceiverUnlinkedEvent {
        private final BlockEntity receiver;
        private final BlockEntity oldController;
        
        public TargetReceiverUnlinkedEvent(BlockEntity receiver, BlockEntity oldController) {
            this.receiver = receiver;
            this.oldController = oldController;
        }
        
        public BlockEntity getReceiver() {
            return receiver;
        }
        
        public BlockEntity getOldController() {
            return oldController;
        }
    }
    
    public static class TargetReceiverTargetSetEvent {
        private final BlockEntity receiver;
        private final BlockEntity targetController;
        private final BlockPos oldPos;
        private final BlockPos targetPos;
        
        public TargetReceiverTargetSetEvent(BlockEntity receiver, BlockEntity targetController, BlockPos oldPos, BlockPos targetPos) {
            this.receiver = receiver;
            this.targetController = targetController;
            this.oldPos = oldPos;
            this.targetPos = targetPos;
        }
        
        public BlockEntity getReceiver() {
            return receiver;
        }
        
        public BlockEntity getTargetController() {
            return targetController;
        }
        
        public BlockPos getOldPos() {
            return oldPos;
        }
        
        public BlockPos getTargetPos() {
            return targetPos;
        }
    }
    
    public static class TargetReceiverFireEvent {
        private final BlockEntity receiver;
        private final BlockEntity targetController;
        private final Player player;
        private final BlockPos targetPos;
        
        public TargetReceiverFireEvent(BlockEntity receiver, BlockEntity targetController, Player player, BlockPos targetPos) {
            this.receiver = receiver;
            this.targetController = targetController;
            this.player = player;
            this.targetPos = targetPos;
        }
        
        public BlockEntity getReceiver() {
            return receiver;
        }
        
        public BlockEntity getTargetController() {
            return targetController;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public BlockPos getTargetPos() {
            return targetPos;
        }
    }
    
    // Registration methods
    public static void registerOnTargetReceiverLinked(Consumer<TargetReceiverLinkedEvent> listener) {
        targetReceiverLinkedListeners.add(listener);
    }
    
    public static void registerOnTargetReceiverUnlinked(Consumer<TargetReceiverUnlinkedEvent> listener) {
        targetReceiverUnlinkedListeners.add(listener);
    }
    
    public static void registerOnTargetReceiverTargetSet(Consumer<TargetReceiverTargetSetEvent> listener) {
        targetReceiverTargetSetListeners.add(listener);
    }
    
    public static void registerOnTargetReceiverFire(Consumer<TargetReceiverFireEvent> listener) {
        targetReceiverFireListeners.add(listener);
    }
    
    // Fire methods
    public static void fireTargetReceiverLinked(BlockEntity receiver, BlockEntity targetController) {
        if (receiver != null && receiver.getLevel() != null && receiver.getLevel().isClientSide) {
            return;
        }
        TargetReceiverLinkedEvent event = new TargetReceiverLinkedEvent(receiver, targetController);
        for (Consumer<TargetReceiverLinkedEvent> listener : targetReceiverLinkedListeners) {
            listener.accept(event);
        }
    }
    
    public static void fireTargetReceiverUnlinked(BlockEntity receiver, BlockEntity oldController) {
        if (receiver != null && receiver.getLevel() != null && receiver.getLevel().isClientSide) {
            return;
        }
        TargetReceiverUnlinkedEvent event = new TargetReceiverUnlinkedEvent(receiver, oldController);
        for (Consumer<TargetReceiverUnlinkedEvent> listener : targetReceiverUnlinkedListeners) {
            listener.accept(event);
        }
    }
    
    public static void fireTargetReceiverTargetSet(BlockEntity receiver, BlockEntity targetController, BlockPos oldPos, BlockPos targetPos) {
        if (receiver != null && receiver.getLevel() != null && receiver.getLevel().isClientSide) {
            return;
        }
        TargetReceiverTargetSetEvent event = new TargetReceiverTargetSetEvent(receiver, targetController, oldPos, targetPos);
        for (Consumer<TargetReceiverTargetSetEvent> listener : targetReceiverTargetSetListeners) {
            listener.accept(event);
        }
    }
    
    public static void fireTargetReceiverFire(BlockEntity receiver, BlockEntity targetController, Player player, BlockPos targetPos) {
        if (receiver != null && receiver.getLevel() != null && receiver.getLevel().isClientSide) {
            return;
        }
        TargetReceiverFireEvent event = new TargetReceiverFireEvent(receiver, targetController, player, targetPos);
        for (Consumer<TargetReceiverFireEvent> listener : targetReceiverFireListeners) {
            listener.accept(event);
        }
    }
    
    // Clear all listeners (useful for cleanup)
    public static void clearAllListeners() {
        targetReceiverLinkedListeners.clear();
        targetReceiverUnlinkedListeners.clear();
        targetReceiverTargetSetListeners.clear();
        targetReceiverFireListeners.clear();
    }
}
