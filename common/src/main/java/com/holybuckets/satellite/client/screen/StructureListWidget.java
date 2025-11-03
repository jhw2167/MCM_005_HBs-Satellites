package com.holybuckets.satellite.client.screen;

import com.holybuckets.foundation.structure.StructureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public class StructureListWidget extends ObjectSelectionList<StructureListWidget.StructureEntry> {
    private final SatelliteScreen parent;
    private final Set<BlockPos> selectedPositions;

    public StructureListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight, int x0, SatelliteScreen parent) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.parent = parent;
        // Set the X position for right-alignment
        this.x0 = x0;
        this.x1 = x0 + width;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);

        selectedPositions = new HashSet<>();
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Override
    public int getRowLeft() {
        return this.x0;
    }

    @Override
    protected void renderBackground(GuiGraphics graphics) {
        // Override to remove the default background - we'll render it in the screen
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
    boolean listClicked = super.mouseClicked( mouseX, mouseY, button);
        if (button == 0 && !listClicked) {
            this.setSelected(null);
            return false;
        }
        return true;
    }

    @Override
    public void renderSelection(GuiGraphics graphics, int top, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        // Override to fix alignment - use correct left position
        int left = this.getRowLeft();
        int right = left + entryWidth;

        // Draw selection outline at correct position
        graphics.renderOutline(left - 1, top - 1, entryWidth + 2, entryHeight + 2, borderColor);
    }

    public void setSelected(StructureEntry entry) {
        super.setSelected(entry);
        if (entry != null) {
            // Notify parent of selection
            // TODO: Update coordinate fields in parent screen
        }
    }

    public void addEntry(StructureInfo structure) {
    if( structure == null || selectedPositions.contains(structure.getOrigin()) ) return;
        this.addEntry(new StructureEntry(structure, this));
    }

    // Entry class for each structure in the list
    public static class StructureEntry extends ObjectSelectionList.Entry<StructureEntry> {
        private final StructureInfo structure;
        private final StructureListWidget list;
        private final Minecraft minecraft;

        public StructureEntry(StructureInfo structure, StructureListWidget list) {
            this.structure = structure;
            this.list = list;
            this.minecraft = Minecraft.getInstance();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            // Background for hover effect
            if (isMouseOver) {
                graphics.fill(left, top, left + width, top + height, 0x80808080);
            }

            // Render structure name (first line)
            Component nameComponent = Component.literal(structure.getCommonName());
            graphics.drawString(
                this.minecraft.font,
                nameComponent,
                left + 5,
                top + 2,
                0xFFFFFF
            );

            // Render coordinates (second line)
            graphics.pose().pushPose();
            graphics.pose().translate(left + 5, top + 12, 0); // Move to position
            graphics.pose().scale(0.8f, 0.8f, 0.8f);                // Then scale
            String coordText = String.format("X: %d, Y: %d, Z: %d",
                structure.getOrigin().getX(),
                structure.getOrigin().getY(),
                structure.getOrigin().getZ()
            );
            Component coordComponent = Component.literal(coordText);
            graphics.drawString(
                this.minecraft.font,
                coordComponent,
                0,
                0,
                0xA0A0A0
            );
            graphics.pose().popPose();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.list.setSelected(this);
                // TODO: Update parent screen coordinates to this structure's position
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(structure.getCommonName() + " at " + structure.getOrigin().toShortString());
        }

        public StructureInfo getStructure() {
            return this.structure;
        }
    }
}