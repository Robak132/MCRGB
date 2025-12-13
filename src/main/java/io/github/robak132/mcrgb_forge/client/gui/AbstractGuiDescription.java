package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.LightweightGuiDescription;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WSprite;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSavedPalettesArea;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSmartTextField;
import io.github.robak132.mcrgb_forge.colors.Color;
import io.github.robak132.mcrgb_forge.colors.Color.ColorModel;
import io.github.robak132.mcrgb_forge.colors.RGB;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGuiDescription extends LightweightGuiDescription {

    static final int SLOTS_HEIGHT = 7;
    static final int SLOTS_WIDTH = 9;
    public final WGridPanel root = new WGridPanel();
    public final WGridPanel mainPanel = new WGridPanel();
    public final WSavedPalettesArea savedPalettesArea = new WSavedPalettesArea(this, SLOTS_WIDTH, SLOTS_HEIGHT);
    public final WSmartTextField hexInput = new WSmartTextField(Component.literal("#FFFFFF"));
    public final WSprite colorDisplay = new WSprite(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rect.png"));
    protected boolean refreshing = false;

    public Color activeColor = new RGB(255, 255, 255);
    public ColorModel activeColorModel = ColorModel.RGB;

    public void setColor(Color color) {
        setColor(color, this.activeColorModel);
    }

    public void setColor(ColorModel model) {
        setColor(this.activeColor, model);
    }

    public void setColor(Color color, ColorModel model) {
        lockWidgets(() -> {
            this.activeColorModel = model;
            this.activeColor = color.toModel(this.activeColorModel);
            hexInput.setText(activeColor.toHexString());
        });
    }

    protected void lockWidgets(Runnable action) {
        lockWidgets(action, null);
    }

    protected void lockWidgets(Runnable action, Consumer<Exception> exceptionHandler) {
        if (refreshing) return;
        refreshing = true;
        try {
            action.run();
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            }
        } finally {
            refreshing = false;
        }
    }
}
