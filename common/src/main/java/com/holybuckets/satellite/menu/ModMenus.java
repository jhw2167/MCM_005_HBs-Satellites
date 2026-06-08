package com.holybuckets.satellite.menu;

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.block.be.TargetControllerBlockEntity;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.menu.BalmMenuFactory;
import net.blay09.mods.balm.api.menu.BalmMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
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
            new BalmMenuFactory<TargetControllerMenu, Object>() {
                @Override
                public TargetControllerMenu create(int i, Inventory inventory, Object object) {
                    BlockPos pos = (BlockPos) object;
                        Level level = inventory.player.level();
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof TargetControllerBlockEntity) {
                        TargetControllerBlockEntity entity = (TargetControllerBlockEntity) be;
                        entity.setLevel(level);
                        return new TargetControllerMenu(i, inventory, entity);
                    }
                    return null;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, Object> getStreamCodec() {
                    return null;
                }

            });
    }

    private static ResourceLocation id(String name) {
        return HBUtil.LOC(Constants.MOD_ID, name);
    }

}
