package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.LightweightGuiDescription;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WSprite;
import io.github.robak132.libgui_forge.widget.WTextField;
import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSavedPalettesArea;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGuiScreen extends LightweightGuiDescription {

    static final int SLOTS_HEIGHT = 7;
    static final int SLOTS_WIDTH = 9;
    public final WGridPanel root = new WGridPanel();
    public final WGridPanel mainPanel = new WGridPanel();
    public final WSavedPalettesArea savedPalettesArea = new WSavedPalettesArea(this, 9, 7);
    public final WTextField hexInput = new WTextField(Component.literal("#FFFFFF"));
    public ColorVector inputColor = new ColorVector(255, 255, 255);
    public final WSprite colorDisplay = new WSprite(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rect.png"));

    public int getColor() {
        String hex = inputColor.getHex().replace("#", "");
        return Integer.parseInt(hex, 16);
    }

    public void setColor(ColorVector color) {
        inputColor = color;
        hexInput.setText(inputColor.getHex());
        colorDisplay.setOpaqueTint(inputColor.asInt());
    }
}
