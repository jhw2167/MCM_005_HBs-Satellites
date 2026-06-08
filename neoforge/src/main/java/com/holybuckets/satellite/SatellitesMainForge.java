package com.holybuckets.satellite;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.neoforge.NeoForgeLoadContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod( Constants.MOD_ID)
public class SatellitesMainForge {

    public SatellitesMainForge(IEventBus modEventBus) {
        super();

        final var context = new NeoForgeLoadContext(modEventBus);
        Balm.initialize(Constants.MOD_ID, context, CommonClass::init);
    }

    /*
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        // Get the block being highlighted
        BlockHitResult target = event.getTarget();
        Level level = event.getCamera().getEntity().level();
        if(SatelliteMain.chiselBitsApi.isViewingHoloBlock(level, target)) {
            event.setCanceled(true);
        }
    }
    */


}
