package com.holybuckets.satellite.event;

import mod.chiselsandbits.client.render.ChiseledBlockWireframeRenderer;
import mod.chiselsandbits.client.logic.MultiStateBlockPreviewRenderHandler;
import mod.chiselsandbits.client.chiseling.preview.render.ConfigurableColoredVoxelShapeChiselContextPreviewRenderer;
import mod.chiselsandbits.voxelshape.VoxelShapeCalculator;

import com.holybuckets.satellite.block.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = com.holybuckets.satellite.Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    //onClientSetup was too early, values were null

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBuildCreativeModeTabsEvent(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.holoBaseBlock, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.holoDarkBlock, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.holoAirBlock, RenderType.cutout()); // Make it invisible

        });
    }

}
