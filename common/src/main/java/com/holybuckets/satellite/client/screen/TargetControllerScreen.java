package com.holybuckets.satellite.client.screen;

import com.holybuckets.satellite.Constants;
import com.holybuckets.satellite.menu.TargetControllerMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TargetControllerScreen extends AbstractContainerScreen<TargetControllerMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Constants.MOD_ID, "textures/gui/target_controller_gui.png");

    public static int INV_HEIGHT = 166; // Same as chest
    public static int INV_WIDTH = 176;  // Same as chest

    public TargetControllerScreen(TargetControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable("block." + Constants.MOD_ID + ".target_controller"));
        this.imageHeight = INV_HEIGHT;
        this.imageWidth = INV_WIDTH;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, Component.translatable("gui." + Constants.MOD_ID + ".target_controller.title"), 8, 6, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("container.inventory"), 8, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
