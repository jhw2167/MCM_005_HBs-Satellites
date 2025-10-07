package com.holybuckets.satellite.client;

import com.holybuckets.satellite.block.ModBlocks;
import net.blay09.mods.balm.api.client.rendering.BalmRenderers;
import net.minecraft.client.renderer.RenderType;

public class ModRenderers {

    //public static ModelLayerLocation someModel;

    public static void clientInitialize(BalmRenderers renderers) {

        renderers.setBlockRenderType(() -> ModBlocks.holoBaseBlock, RenderType.cutout() );
        renderers.setBlockRenderType(() -> ModBlocks.holoBaseBlock, RenderType.translucent());
        renderers.setBlockRenderType(() -> ModBlocks.holoDarkBlock, RenderType.translucent());
        //waystoneModel = renderers.registerModel(new ResourceLocation(Waystones.MOD_ID, "waystone"), () -> WaystoneModel.createLayer(CubeDeformation.NONE));
        //renderers.setBlockRenderType(() -> ModBlocks.stoneBrickBlockEntity, RenderType.cutout());
    }

}
