package com.bacco.gui;

import com.bacco.libgui.widget.TooltipBuilder;
import com.bacco.libgui.widget.WPlainPanel;
import com.bacco.Palette;
import com.bacco.libgui.client.BackgroundPainter;
import com.bacco.libgui.widget.data.HorizontalAlignment;
import com.bacco.libgui.widget.icon.TextureIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public class WPaletteWidget extends WPlainPanel {
    int slotsWidth = 9;
    ArrayList<WColourPreviewIcon> savedColours = new ArrayList<>();
    Palette palette;
    MCRGBBaseGui cg;
    ResourceLocation colourIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "square.png");
    ResourceLocation editIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "edit.png");
    TextureIcon editIcon = new TextureIcon(editIdentifier);
    WSmallButton editButton = new WSmallButton(editIcon) {
        @Override
        public void addTooltip(TooltipBuilder tooltip) {
            tooltip.add(Component.translatable("ui.mcrgb_forge.edit_palette_info"));
            super.addTooltip(tooltip);
        }
    };
    ResourceLocation deleteIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "delete.png");
    TextureIcon deleteIcon = new TextureIcon(deleteIdentifier);

    WSmallButton deleteButton = new WSmallButton(deleteIcon){
        @Override
        public void addTooltip(TooltipBuilder tooltip) {
            tooltip.add(Component.translatable("ui.mcrgb_forge.delete_palette_info"));
            super.addTooltip(tooltip);
        }
    };

    public WPaletteWidget(){
        // Default constructor
    }

    public void buildPaletteWidget(MCRGBBaseGui cg){

        this.setBackgroundPainter(BackgroundPainter.createColorful(0xFFFFFF));
        for(int i = 0; i < slotsWidth; i++) {
            savedColours.add(new WColourPreviewIcon(colourIdentifier,cg));
            savedColours.get(i).setInteractable(false);
            this.add(savedColours.get(i), i*17, 0, 18, 18);
        }
        this.add(editButton,(int)(8.6f*18),0,10,10);
        editButton.setSize(10,10);
        editButton.setIconSize(9);
        editButton.setAlignment(HorizontalAlignment.LEFT);
        editButton.setOnClick(() -> cg.savedPalettesArea.editPalette(this));

        this.add(deleteButton,(int)(8.6f*18),9,1,1);
        deleteIcon.setColor(0xFF_FC5454);
        deleteButton.setSize(10,10);
        deleteButton.setIconSize(9);
        deleteButton.setAlignment(HorizontalAlignment.LEFT);
        deleteButton.setOnClick(() -> cg.savedPalettesArea.deletePalette(this));
    }
    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        if(cg.savedPalettesArea.editingPalette == this) {
            context.fill(x,y,this.width,this.height,0xFF00ff00);
        }
    }
}
