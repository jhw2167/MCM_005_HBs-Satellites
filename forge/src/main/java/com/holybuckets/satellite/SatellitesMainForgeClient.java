package com.holybuckets.satellite;

import com.holybuckets.satellite.client.CommonClassClient;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SatellitesMainForgeClient {


    public static void clientInitializeForge() {
        CommonClassClient.initClient();
        //Item challengeChest = ModBlocks.challengeChest.asItem();
       // setBlockEntityRender( challengeChest, ChallengeItemBlockRenderer.CHEST_RENDERER);
    }

//        private static void setBlockEntityRender(Object item, BlockEntityWithoutLevelRenderer renderer) {
//            ((IBewlrRenderer) item).setBlockEntityWithoutLevelRenderer(renderer);
//        }

}
