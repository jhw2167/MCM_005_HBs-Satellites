package com.holybuckets.satellite.particle;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;


public class WoolDustHelper {
    private static final IntObjectHashMap<DustParticleOptions> WOOL_DUST_CACHE = new IntObjectHashMap<>();

    public static void addDustColorFromWool(Block wool, int id)
    {
        DyeColor color = DyeColor.WHITE;
        for(DyeColor dyeColor : DyeColor.values()) {
            if(dyeColor.getMapColor() == wool.defaultMapColor()) {
                color = dyeColor;
                break;
            }
        }
        float[] rgb = color.getTextureDiffuseColors();
        WOOL_DUST_CACHE.put(id, new DustParticleOptions(
            new Vector3f(rgb[0], rgb[1], rgb[2]),
            1.0f
        ));
    }

    public static DustParticleOptions getDust(int id) {
        return WOOL_DUST_CACHE.get(id);
    }

    public static int getIntColor(int id) {
        Vector3f v = getDust(id).getColor();
        //(red << 16) | (green << 8) | blue;
        return ((int)(v.x * 255) << 16) | ((int)(v.y * 255) << 8) | (int)(v.z * 255);
    }

    public static float[] getWoolColorRGB(int colorId) {
        Vector3f v = getDust(colorId).getColor();
        return new float[] {v.x, v.y, v.z};
    }
}
