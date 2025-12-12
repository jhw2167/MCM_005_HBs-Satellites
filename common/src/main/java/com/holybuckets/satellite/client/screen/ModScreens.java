package com.holybuckets.satellite.client.screen;

import com.holybuckets.satellite.menu.ModMenus;
import net.blay09.mods.balm.api.client.screen.BalmScreens;

public class ModScreens {

    public static void clientInitialize(BalmScreens screens) {
        screens.registerScreen(
            ModMenus.targetControllerMenu::get,
            TargetControllerScreen::new
        );
    }
}
