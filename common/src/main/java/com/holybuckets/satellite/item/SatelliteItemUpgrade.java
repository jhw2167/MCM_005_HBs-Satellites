package com.holybuckets.satellite.item;

import net.minecraft.resources.ResourceLocation;
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
    private final String id;
    private final ResourceLocation spriteRef;

    public SatelliteItemUpgrade(String id) {
        super(Balm.getItems().itemProperties());
        this.id = id;
        this.spriteRef = new ResourceLocation("hbs_satellites", "item/" + id);
    }

    public ResourceLocation getUpgradeSpriteLocation() {
        return spriteRef;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        //super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add( Component.translatable("item.hbs_satellites."+id+".desc"));
    }
}
