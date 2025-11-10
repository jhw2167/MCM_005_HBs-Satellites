package com.holybuckets.satellite.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.blay09.mods.balm.api.Balm;

import javax.annotation.Nullable;
import java.util.List;

public class SatelliteItemUpgrade extends Item {
    private final DyeColor dyeColor;

    public SatelliteItemUpgrade(DyeColor dyeColor) {
        super(Balm.getItems().itemProperties());
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.hbs_satellites.upgrade.tooltip", dyeColor.getName()));
    }
}
