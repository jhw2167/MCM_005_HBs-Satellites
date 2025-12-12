package com.holybuckets.satellite.menu;

import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.menu.BalmMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ModMenus {

    public static DeferredObject<MenuType<TemplateChestEntityMenu>> countingChestMenu;
    public static DeferredObject<MenuType<TargetControllerMenu>> targetControllerMenu;


    public static void initialize(BalmMenus menus)
    {
        /*
        countingChestMenu = menus.registerMenu(id("counting_chest_menu"),
            (syncId, inventory, buf) -> {
                BlockPos pos = buf.readBlockPos();
                Level level = inventory.player.level();
                BlockEntity be = inventory.player.level().getBlockEntity(pos);
                if( be instanceof TemplateBlockEntity) {
                    TemplateBlockEntity cbe = (TemplateBlockEntity) be;
                    cbe.setLevel(level);
                    return new TemplateChestEntityMenu(syncId, inventory, cbe);
                }
                return null;
            });
         */

        targetControllerMenu = menus.registerMenu(id("target_controller_menu"),
            (syncId, inventory, buf) -> {
                BlockPos pos = buf.readBlockPos();
                Level level = inventory.player.level();
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TargetControllerBlockEntity) {
                    TargetControllerBlockEntity entity = (TargetControllerBlockEntity) be;
                    entity.setLevel(level);
                    return new TargetControllerMenu(syncId, inventory, entity);
                }
                return null;
            });
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
