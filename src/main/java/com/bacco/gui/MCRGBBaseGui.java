package com.bacco.gui;

import com.bacco.libgui.client.LightweightGuiDescription;
import com.bacco.ColourVector;
import com.bacco.MCRGBClient;
import com.bacco.libgui.widget.WGridPanel;
import com.bacco.libgui.widget.WSprite;
import com.bacco.libgui.widget.WTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MCRGBBaseGui extends LightweightGuiDescription {

    public WGridPanel root = new WGridPanel();
    public WGridPanel mainPanel = new WGridPanel();

    public WSavedPalettesArea savedPalettesArea;

    WTextField hexInput = new WTextField(Component.literal("#FFFFFF"));

    ColourVector inputColour = new ColourVector(255, 255, 255);

    public Minecraft client;
    public MCRGBClient mcrgb_forgeClient;

    WSprite colourDisplay = new WSprite(ResourceLocation.tryBuild("mcrgb_forge", "rect.png"));

    MCRGBBaseGui() {
    }

    public int getColour() {
        String hex = inputColour.getHex().replace("#", "");
        return Integer.parseInt(hex, 16);
    }

    public void setColour(ColourVector colour) {
        inputColour = colour;
        hexInput.setText(inputColour.getHex());
        colourDisplay.setOpaqueTint(inputColour.asInt());
    }
}
