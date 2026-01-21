package com.holybuckets.satellite.client.screen;

import com.google.gson.JsonObject;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.console.IMessager;
import com.holybuckets.foundation.console.Messager;
import com.holybuckets.foundation.networking.SimpleStringMessage;
import com.holybuckets.foundation.structure.StructureInfo;
import com.holybuckets.foundation.structure.StructureManager;
import com.holybuckets.satellite.CommonClass;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.holybuckets.satellite.block.be.SatelliteControllerBlockEntity;
import com.holybuckets.satellite.block.be.isatelliteblocks.ISatelliteBE;
import com.holybuckets.satellite.config.ModConfig;
import com.holybuckets.satellite.core.SatelliteManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SatelliteScreen extends Screen {

    private static final Component TITLE = Component.literal("Satellite Controller");
    private static final Component X_LABEL = Component.literal("X:");
    private static final Component Y_LABEL = Component.literal("Y:");
    private static final Component Z_LABEL = Component.literal("Z:");

    private final ISatelliteBE satelliteBlock;

    // Left column widgets
    private EditBox xEdit;
    private EditBox yEdit;
    private EditBox zEdit;
    private Button exitButton;
    private Button launchButton;

    private int currentX, currentY, currentZ;

    // Right column widgets
    private StructureListWidget structureList;

    // Layout constants
    private static final float LEFT_COLUMN_WIDTH = 0.30f;
    private static final float RIGHT_COLUMN_WIDTH = 0.70f;
    private static final float COORD_SECTION_HEIGHT = 0.40f;
    private static final float INVENTORY_SECTION_HEIGHT = 0.30f;
    private static final float BUTTON_SECTION_HEIGHT = 0.30f;
    private static final float RIGHT_LIST_HEIGHT = 0.60f;
    private static final float RIGHT_INVENTORY_HEIGHT = 0.40f;

    private int leftColumnX;
    private int leftColumnWidth;
    private int rightColumnX;
    private int rightColumnWidth;
    private int guiTop;
    private int guiHeight;
    private int guiWidth;  // Total GUI width
    private int guiLeft;   // Left edge of GUI

    public SatelliteScreen(ISatelliteBE satelliteBlock) {
        super(TITLE);
        this.satelliteBlock = satelliteBlock;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate layout dimensions - center GUI at 60% screen width
        this.guiWidth = (int)(this.width * 0.7f);
        this.guiLeft = (this.width - this.guiWidth) / 2;  // Center horizontally
        this.guiHeight = this.height - 60; // Leave more margin for title + current position
        this.guiTop = 50; // Start lower to accommodate title and current position

        this.leftColumnWidth = (int)(this.guiWidth * LEFT_COLUMN_WIDTH);
        this.rightColumnWidth = (int)(this.guiWidth * RIGHT_COLUMN_WIDTH);
        this.leftColumnX = this.guiLeft + 10;
        this.rightColumnX = this.leftColumnX + this.leftColumnWidth + 10; // 10px gap

        initLeftColumn();
        initRightColumn();
    }

    private void initLeftColumn() {
        int sectionHeight = (int)(guiHeight * COORD_SECTION_HEIGHT);
        int yStart = guiTop + 15; // Add space for "Target Position" label

        // Coordinate input boxes (top 40% of left column)
        int labelWidth = 20;
        int editBoxWidth = leftColumnWidth - labelWidth - 10;
        int boxHeight = 20;
        int verticalBuffer = 8; // Buffer between boxes
        int availableSpace = sectionHeight - 15 - (3 * boxHeight); // Space minus label and boxes
        int spacing = availableSpace / 4; // Divide remaining space

        // X coordinate
        this.xEdit = new EditBox(this.font,
            leftColumnX + labelWidth,
            yStart + spacing,
            editBoxWidth,
            boxHeight,
            Component.literal("X"));
        this.currentX = satelliteBlock.getTargetPos().getX();
        this.xEdit.setValue(String.valueOf(currentX));
        this.xEdit.setMaxLength(10);
        this.xEdit.setBordered(true);
        this.xEdit.setEditable(true);
        this.addRenderableWidget(this.xEdit);

        // Y coordinate
        this.yEdit = new EditBox(this.font,
            leftColumnX + labelWidth,
            yStart + spacing + boxHeight + verticalBuffer,
            editBoxWidth,
            boxHeight,
            Component.literal("Y"));
        this.currentY = satelliteBlock.getTargetPos().getY();
        this.yEdit.setValue(String.valueOf(currentY));
        this.yEdit.setMaxLength(10);
        this.yEdit.setBordered(true);
        this.yEdit.setEditable(true);
        this.addRenderableWidget(this.yEdit);

        // Z coordinate
        this.zEdit = new EditBox(this.font,
            leftColumnX + labelWidth,
            yStart + spacing + (boxHeight + verticalBuffer) * 2,
            editBoxWidth,
            boxHeight,
            Component.literal("Z"));
        this.currentZ = satelliteBlock.getTargetPos().getZ();
        this.zEdit.setValue(String.valueOf(currentZ));
        this.zEdit.setMaxLength(10);
        this.zEdit.setBordered(true);
        this.zEdit.setEditable(true);
        this.addRenderableWidget(this.zEdit);

        // Inventory section (middle 30% - left empty for now)
        int errorStart = yStart + sectionHeight;
        int errorMsgHeight = (int)(guiHeight * INVENTORY_SECTION_HEIGHT);



        // Button section (bottom 30%)
        int buttonSectionY = errorStart + errorMsgHeight;
        int buttonSectionHeight = (int)(guiHeight * BUTTON_SECTION_HEIGHT);
        int buttonSpacing = buttonSectionHeight / 3;

        // Launch button
        this.launchButton = Button.builder(Component.literal("Move"), button -> {
            this.onLaunch();
        }).bounds(
            leftColumnX,
            buttonSectionY + buttonSpacing / 2,
            leftColumnWidth,
            20
        ).build();
        this.addRenderableWidget(this.launchButton);

        // Exit button
        this.exitButton = Button.builder(Component.literal("Exit"), button -> {
            this.onClose();
        }).bounds(
            leftColumnX,
            buttonSectionY + buttonSpacing + buttonSpacing / 2,
            leftColumnWidth,
            20
        ).build();
        this.addRenderableWidget(this.exitButton);
    }

    private void initRightColumn() {
        int listHeight = (int)(guiHeight * RIGHT_LIST_HEIGHT);
        int listTop = guiTop + 15; // Align with coordinate boxes (same as yStart in initLeftColumn)

        // Structure list (top 60% of right column)
        // Position it at rightColumnX and with proper width
        this.structureList = new StructureListWidget(
            this.minecraft,
            rightColumnWidth - 20,
            listHeight,
            listTop,
            listTop + listHeight,
            25, // Item height
            rightColumnX,  // Pass X position to widget
            this
        );

        // Populate with structures
        List<StructureInfo> structures = getStructureList(satelliteBlock.getBlockPos());
        for (StructureInfo info : structures) {
            this.structureList.addEntry(info);
        }

        this.addRenderableWidget(this.structureList);

        // Inventory section (bottom 40% - left empty for now)
        int inventoryY = listTop + listHeight + 10;
        int inventoryHeight = (int)(guiHeight * RIGHT_INVENTORY_HEIGHT);
        // TODO: Add inventory rendering here later
    }

    @Override
    public void tick() {
        super.tick();
        if(this.structureList != null) {
            if(this.structureList.getSelected() != null && this.structureList.getSelected().isFocused()) {
                StructureListWidget.StructureEntry widget = this.structureList.getSelected();
                BlockPos pos = widget.getStructure().getOrigin();
                currentX = pos.getX();
                currentY = pos.getY();
                currentZ = pos.getZ();
            }
        }
        if(!this.xEdit.isFocused())
            this.xEdit.setValue(String.valueOf( currentX ));
        if(!this.yEdit.isFocused())
            this.yEdit.setValue(String.valueOf( currentY ));
        if(!this.zEdit.isFocused())
            this.zEdit.setValue(String.valueOf( currentZ ));

        this.xEdit.tick();
        this.yEdit.tick();
        this.zEdit.tick();

        try { this.currentX = Integer.parseInt(this.xEdit.getValue()); } catch (NumberFormatException e) {}
        try { this.currentY = Integer.parseInt(this.yEdit.getValue()); } catch (NumberFormatException e) {}
        try { this.currentZ = Integer.parseInt(this.zEdit.getValue()); } catch (NumberFormatException e) {}

}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Render title
        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2f, 10f, 0f);
        graphics.pose().scale(1.3f, 1.3f, 1.3f);
        graphics.drawCenteredString(this.font, this.title, 0, 0, 0xFFFFFF);
        graphics.pose().popPose();

        // Render current position under title
        BlockPos currentPos = satelliteBlock.getBlockPos();
        String currentPosText = String.format("Current Position: %d, %d, %d",
            currentPos.getX(), currentPos.getY(), currentPos.getZ());
        graphics.drawCenteredString(this.font, Component.literal(currentPosText),
            this.width / 2, 24, 0x808080);

            //Just under this lets render a bright green "En Route To:"
        BlockPos targetPos = satelliteBlock.getTargetPos();
        if(satelliteBlock.isTraveling() && targetPos != null && !targetPos.equals( currentPos ))
        {
            graphics.pose().pushPose();
            graphics.pose().translate(this.width / 2, 36, 0);
            graphics.pose().scale(.5f, .5f, .5f);
            String targetPosText = String.format("Traveling To: %d, %d, %d",
                targetPos.getX(), targetPos.getY(), targetPos.getZ());
            graphics.drawCenteredString(this.font, Component.literal(targetPosText),
                0, 0, 0x00FF00);
            graphics.pose().popPose();
        }

        // Render section titles
        graphics.drawString(this.font, Component.literal("Target Position"),
            leftColumnX, guiTop, 0xFFFFFF);
        graphics.drawString(this.font, Component.literal("Structures"),
            rightColumnX, guiTop, 0xFFFFFF);

        // Render coordinate labels
        int sectionHeight = (int)(guiHeight * COORD_SECTION_HEIGHT);
        int yStart = guiTop + 15;
        int boxHeight = 20;
        int verticalBuffer = 8;
        int availableSpace = sectionHeight - 15 - (3 * boxHeight);
        int spacing = availableSpace / 4;

        graphics.drawString(this.font, X_LABEL, leftColumnX, yStart + spacing + 5, 0xA0A0A0);
        graphics.drawString(this.font, Y_LABEL, leftColumnX, yStart + spacing + boxHeight + verticalBuffer + 5, 0xA0A0A0);
        graphics.drawString(this.font, Z_LABEL, leftColumnX, yStart + spacing + (boxHeight + verticalBuffer) * 2 + 5, 0xA0A0A0);

        // Render structure list outline (right-aligned)
        int listHeight = (int)(guiHeight * RIGHT_LIST_HEIGHT);
        int listTop = guiTop + 15; // Align with coordinate boxes
        graphics.fill(rightColumnX - 2, listTop - 2, rightColumnX + rightColumnWidth - 18, listTop + listHeight + 2, 0x80000000);
        graphics.renderOutline(rightColumnX - 2, listTop - 2, rightColumnWidth - 16, listHeight + 4, 0xFFFFFFFF);

        String errorMsg = satelliteBlock.getSatelliteDisplayError();
        if(errorMsg != null) {
            int listCenterX = rightColumnX + (rightColumnWidth - 18) / 2;
            int errorY = listTop + listHeight + 6;
            graphics.drawCenteredString(this.font, Component.literal(errorMsg),
                listCenterX, errorY, 0xFFFF5555); // Red text (or use 0xFFAA0000 for darker red)
        }


        // Render all widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void onClose() {

        try {
            int x = Integer.parseInt(this.xEdit.getValue());
            int y = Integer.parseInt(this.yEdit.getValue());
            int z = Integer.parseInt(this.zEdit.getValue());
            satelliteBlock.setTargetPos( new BlockPos(x, y, z) );

            // Send to server
            // TODO: Implement network packet to update controller

        } catch (NumberFormatException e) {
            // Invalid input - could add error message here
        }

        super.onClose();
    }

    private void onLaunch() {
        this.onClose();
        //Create a json message with the satellites colorId and targetPos
        BlockPos target = satelliteBlock.getTargetPos();
        if(target == null) {
            msg("Satellite target position is not set"); return;
        }
        if(target.equals( satelliteBlock.getBlockPos() )) {
            msg("Satellite target position is the same as current position"); return;
        }
        if(satelliteBlock.getColorId() == -1) {
            msg("Satellite color ID is not set.  Right click the sides of the block"); return;
        }
        satelliteBlock.launch(target);
        JsonObject json = new JsonObject();
        json.addProperty("colorId", satelliteBlock.getColorId());
        json.addProperty("targetPos", HBUtil.BlockUtil.positionToString(satelliteBlock.getTargetPos()));
        SimpleStringMessage.createAndFire(SatelliteManager.MSG_ID_TARGET_POS, json.toString());
    }
        private void msg(String s) {
            IMessager.getInstance().sendBottomActionHint(Minecraft.getInstance().player, s);
        }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Temporary structure data - replace with actual data source
    private List<StructureInfo> getStructureList(BlockPos center) {
        Level level = this.satelliteBlock.getLevel();  //HBUtil.LevelUtil.toLevel(HBUtil.LevelUtil.LevelNameSpace.SERVER, satelliteBlock.getLevel().dimension());
        StructureManager manager = StructureManager.get(level);
        if(manager == null) return new ArrayList<>();
        Set<ResourceLocation> targetStructures = ModConfig.getTrackedStructures();
        //final Registry<StructureType<?>> structureTypes = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.STRUCTURE_TYPE);
        return  manager.getNearestWhitelistedStructures( targetStructures, center, 128);
    }

}