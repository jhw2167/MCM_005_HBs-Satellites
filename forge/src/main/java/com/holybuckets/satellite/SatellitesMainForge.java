package com.holybuckets.satellite;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod( Constants.MOD_ID)
public class SatellitesMainForge {

    public SatellitesMainForge() {
        super();
        Balm.initialize(Constants.MOD_ID, CommonClass::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BalmClient.initialize(Constants.MOD_ID, CommonClass::initClient));
        
        // Register the block highlight event handler
        MinecraftForge.EVENT_BUS.addListener(ChiselBitsAPI::onRenderBlockHighlight);
    }


}
