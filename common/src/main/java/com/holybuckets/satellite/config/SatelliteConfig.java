package com.holybuckets.satellite.config;

import com.holybuckets.satellite.Constants;
import net.blay09.mods.balm.api.config.reflection.Comment;
import net.blay09.mods.balm.api.config.reflection.Config;
import net.blay09.mods.balm.api.config.reflection.NestedType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Config(Constants.MOD_ID)
public class SatelliteConfig {

    @Comment("Display refresh rate in ticks")
    public int displayRefreshRate = 60;

    @Comment("Display refresh rate for chunks currently inhabited by a player in ticks")
    public int displayPlayerRefreshRate = 10;

    @Comment("Entity refresh rate in ticks")
    public int entityRefreshRate = 5;

    @Comment("Controller path refresh rate in ticks")
    public int controllerPathRefreshRate = 200;

    @NestedType(String.class)
    @Comment("Entity types produce green ping on the satellite display")
    public List<String> friendlyEntityTypes = Arrays.asList();

    @Comment("Registers all mobs defined as hostile as red ping")
    public boolean addAllHostileMobsAsRedPing = true;

    @NestedType(String.class)
    @Comment("Entity types produce red ping on the satellite display")
    public Set<String> hostileEntityTypes = Set.of("minecraft:zombie", "minecraft:zombie_villager", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider", "minecraft:enderman", "minecraft:wither_skeleton", "minecraft:husk", "minecraft:stray", "minecraft:drowned", "minecraft:phantom", "minecraft:evoker", "minecraft:vindicator", "minecraft:ravager", "minecraft:pillager", "minecraft:witch", "minecraft:blaze", "minecraft:magma_cube", "minecraft:ghast", "minecraft:endermite", "minecraft:silverfish", "minecraft:cave_spider");

    @NestedType(String.class)
    @Comment("Entity types produce a white ping")
    public Set<String> neutralEntityTypes = Set.of("minecraft:villager", "minecraft:iron_golem");

    @NestedType(String.class)
    @Comment("Entity types only produce a white ping when grouped in large quantities")
    public Set<String> herdEntityTypes = Set.of( "minecraft:cow", "minecraft:sheep", "minecraft:chicken", "minecraft:pig", "minecraft:horse", "minecraft:donkey", "minecraft:llama", "minecraft:wolf", "minecraft:cat");

    @Comment("Minimum count of entities to be considered a herd")
    public int minHerdCountThreshold = 5;


}
