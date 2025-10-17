package com.holybuckets.satellite.config;

import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.SatelliteMain;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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

    public static void init(EventRegistrar reg) {
        reg.registerOnBeforeServerStarted(ModConfig::onServerStarted, EventPriority.High);
    }

    private static void onServerStarted(ServerStartingEvent event)
    {
        friendlyEntities.clear();
        hostileEntities.clear();
        neutralEntities.clear();
        herdEntities.clear();

        Registry<EntityType<?>> registry = event.getServer().registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        SatelliteConfig config = SatelliteMain.CONFIG;
        
        // Convert friendly entities
        for (String entityId : config.friendlyEntityTypes) {
            addEntity(entityId, registry, friendlyEntities);
        }

        // Convert hostile entities
        for (String entityId : config.hostileEntityTypes) {
            addEntity(entityId, registry, hostileEntities);
        }
        // Convert neutral entities
        for (String entityId : config.neutralEntityTypes) {
            addEntity(entityId, registry, neutralEntities);
        }
        // Convert herd entities
        for (String entityId : config.herdEntityTypes) {
            addEntity(entityId, registry, herdEntities);
        }
    }

        private static void addEntity(String entityId, Registry<EntityType<?>> registry, Set<EntityType<?>> targetSet) {
            String[] parts = entityId.split(":");
            EntityType<?> type;
            if (parts.length == 2) {
              type = registry.get(new ResourceLocation(parts[0], parts[1]));
            } else {
                type = registry.get(new ResourceLocation("minecraft", parts[0]));
            }
            if (type != null) {
                targetSet.add(type);
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
