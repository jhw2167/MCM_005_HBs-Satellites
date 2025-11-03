package com.holybuckets.satellite.config;

import com.holybuckets.foundation.GeneralConfig;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.SatelliteMain;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    private static Set<EntityType<?>> friendlyEntities = new HashSet<>();
    private static Set<EntityType<?>> hostileEntities = new HashSet<>();
    private static Set<EntityType<?>> neutralEntities = new HashSet<>();
    private static Set<EntityType<?>> herdEntities = new HashSet<>();

    private static Set<ResourceLocation> trackedStructures = new HashSet<>();

    public static void init(EventRegistrar reg) {
        reg.registerOnBeforeServerStarted( e -> loadDynamicConfigs(e.getServer().registryAccess()), EventPriority.High);
    }

    public static void onConnectedToServer(Level level) {
        if(GeneralConfig.getInstance().isIntegrated()) return;
        loadDynamicConfigs(level.registryAccess());
    }

    private static void loadDynamicConfigs(RegistryAccess reg)
    {
        //** ENTITES

        friendlyEntities.clear();
        hostileEntities.clear();
        neutralEntities.clear();
        herdEntities.clear();

        Registry<EntityType<?>> registry = reg.registryOrThrow(Registries.ENTITY_TYPE);
        SatelliteConfig config = SatelliteMain.CONFIG;
        
        // Convert friendly entities
        for (String entityId : config.entityPings.friendlyEntityTypes) {
            addEntity(entityId, registry, friendlyEntities);
        }

        // Convert hostile entities
        for (String entityId : config.entityPings.hostileEntityTypes) {
            addEntity(entityId, registry, hostileEntities);
        }
        // Convert neutral entities
        for (String entityId : config.entityPings.neutralEntityTypes) {
            addEntity(entityId, registry, neutralEntities);
        }
        // Convert herd entities
        for (String entityId : config.entityPings.herdEntityTypes) {
            addEntity(entityId, registry, herdEntities);
        }

        //STRUCTURES
        trackedStructures.clear();

        //Registry<StructureType<?>> structureRegistry = reg.registryOrThrow(Registries.STRUCTURE_TYPE);
        for (String structId : config.satelliteConfig.satelliteStructureTargetOptions ) {
            //StructureType<?> type = structureRegistry.get(loc);
            ResourceLocation loc = new ResourceLocation(structId);
            if (loc != null) trackedStructures.add(loc);
            else LoggerProject.logWarning("010001", "Satellite Config: Could not find structure type for id: " + structId);
        }

    }

    private static void addEntity(String entityId, Registry<EntityType<?>> registry, Set<EntityType<?>> targetSet) {
        EntityType<?>  type = registry.get(new ResourceLocation(entityId));
        if (type != null) targetSet.add(type);
        else LoggerProject.logWarning("010000", "Satellite Config: Could not find entity type for id: " + entityId);
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

    public static Set<ResourceLocation> getTrackedStructures() {
        return trackedStructures;
    }
}
