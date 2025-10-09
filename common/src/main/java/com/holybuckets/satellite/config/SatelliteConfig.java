package com.holybuckets.satellite.config;

import net.minecraft.world.entity.EntityType;
import net.blay09.mods.balm.api.config.BalmConfigData;
import net.blay09.mods.balm.api.config.Comment;
import net.blay09.mods.balm.api.config.Config;

@Config("hbs_satellites")
public class SatelliteConfig implements BalmConfigData {

    @Comment("Display refresh rate in ticks")
    public int displayRefreshRate = 60;

    @Comment("Display refresh rate for player interactions in ticks") 
    public int displayPlayerRefreshRate = 10;

    @Comment("Entity refresh rate in ticks")
    public int entityRefreshRate = 5;

    @Comment("Controller path refresh rate in ticks")
    public int controllerPathRefreshRate = 200;

    @Comment("Maximum ping time for entity types in seconds")
    public int maxEntityTypePing = 300;

    @Comment("Entity types that show as friendly (green) particles")
    public String[] friendlyEntityTypes = {
        "minecraft:villager",
        "minecraft:iron_golem",
        "minecraft:cat",
        "minecraft:wolf"
    };

    @Comment("Entity types that show as neutral (yellow) particles") 
    public String[] neutralEntityTypes = {
        "minecraft:pig",
        "minecraft:cow",
        "minecraft:sheep",
        "minecraft:chicken"
    };

    @Comment("Entity types that show as enemy (red) particles")
    public String[] enemyEntityTypes = {
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:creeper",
        "minecraft:spider"
    };
}
