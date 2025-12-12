package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.LightweightGuiDescription;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WSprite;
import io.github.robak132.libgui_forge.widget.WTextField;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSavedPalettesArea;
import io.github.robak132.mcrgb_forge.colors.Color;
import io.github.robak132.mcrgb_forge.colors.Color.ColorModel;
import io.github.robak132.mcrgb_forge.colors.RGB;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGuiDescription extends LightweightGuiDescription {

    static final int SLOTS_HEIGHT = 7;
    static final int SLOTS_WIDTH = 9;
    public final WGridPanel root = new WGridPanel();
    public final WGridPanel mainPanel = new WGridPanel();
    public final WSavedPalettesArea savedPalettesArea = new WSavedPalettesArea(this, SLOTS_WIDTH, SLOTS_HEIGHT);
    public final WTextField hexInput = new WTextField(Component.literal("#FFFFFF"));
    public Color inputColor = new RGB(255, 255, 255);
    public final WSprite colorDisplay = new WSprite(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rect.png"));

    public int getColor() {
        return inputColor.argb();
    }

    public void setColor(Color color) {
        inputColor = color;
        hexInput.setText(inputColor.toHexString());
        colorDisplay.setOpaqueTint(inputColor.argb());
    }
}
