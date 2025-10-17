package com.holybuckets.satellite.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    private static Set<EntityType<?>> friendlyEntities = new HashSet<>();
    private static Set<EntityType<?>> hostileEntities = new HashSet<>();
    private static Set<EntityType<?>> neutralEntities = new HashSet<>();
    private static Set<EntityType<?>> herdEntities = new HashSet<>();

    public static void initializeEntitySets(ServerLevel level) {
        friendlyEntities.clear();
        hostileEntities.clear();
        neutralEntities.clear();
        herdEntities.clear();

        var registry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        
        // Convert friendly entities
        for (String entityId : SatelliteConfig.getInstance().friendlyEntityTypes) {
            String[] parts = entityId.split(":");
            if (parts.length == 2) {
                EntityType<?> type = registry.get(new ResourceLocation(parts[0], parts[1]));
                if (type != null) {
                    friendlyEntities.add(type);
                }
            }
        }

        // Convert hostile entities
        for (String entityId : SatelliteConfig.getInstance().hostileEntityTypes) {
            String[] parts = entityId.split(":");
            if (parts.length == 2) {
                EntityType<?> type = registry.get(new ResourceLocation(parts[0], parts[1]));
                if (type != null) {
                    hostileEntities.add(type);
                }
            }
        }

        // Convert neutral entities
        for (String entityId : SatelliteConfig.getInstance().neutralEntityTypes) {
            String[] parts = entityId.split(":");
            if (parts.length == 2) {
                EntityType<?> type = registry.get(new ResourceLocation(parts[0], parts[1]));
                if (type != null) {
                    neutralEntities.add(type);
                }
            }
        }

        // Convert herd entities
        for (String entityId : SatelliteConfig.getInstance().herdEntityTypes) {
            String[] parts = entityId.split(":");
            if (parts.length == 2) {
                EntityType<?> type = registry.get(new ResourceLocation(parts[0], parts[1]));
                if (type != null) {
                    herdEntities.add(type);
                }
            }
        }
    }

    public static Set<EntityType<?>> getFriendlyEntities() {
        return friendlyEntities;
    }

    public static Set<EntityType<?>> getHostileEntities() {
        return hostileEntities;
    }

    public static Set<EntityType<?>> getNeutralEntities() {
        return neutralEntities;
    }

    public static Set<EntityType<?>> getHerdEntities() {
        return herdEntities;
    }
}
