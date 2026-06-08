package com.holybuckets.satellite.event;

import com.holybuckets.satellite.block.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = com.holybuckets.satellite.Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventsModBus {

    //onClientSetup was too early, values were null

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBuildCreativeModeTabsEvent(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.holoBaseBlock, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.holoDarkBlock, RenderType.translucent());
            //ItemBlockRenderTypes.setRenderLayer(ModBlocks.holoAirBlock, RenderType.translucent() ); // Make it invisible
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.satelliteBlock, RenderType.cutout());

        });
    }

}
