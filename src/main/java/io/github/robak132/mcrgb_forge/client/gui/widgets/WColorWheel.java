package io.github.robak132.mcrgb_forge.client.gui.widgets;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.client.ScreenDrawing;
import io.github.robak132.libgui_forge.widget.data.InputResult;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class WColorWheel extends WPickableTexture {

    int cursorX = (width) / 2;
    int cursorY = (height) / 2;
    boolean beenClicked = false;

    public WColorWheel(ResourceLocation image, float u1, float v1, float u2, float v2, AbstractGuiScreen parentGui) {
        super(image, u1, v1, u2, v2, parentGui);
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "circle4.png");
        if (!beenClicked) {
            cursorX = (width) / 2;
            cursorY = (height) / 2;
        }

        ScreenDrawing.texturedRect(context, cursorX + x - 4, cursorY + y - 4, 8, 8, texture, 0xFFFFFFFF);
    }

    @Override
    public InputResult onClick(int containerX, int containerY, int button) {
        beenClicked = true;
        InputResult ret = super.onClick(containerX, containerY, button);
        if (isTransparent) return ret;
        if (!(containerX < 0 || containerY < 0 || containerX >= width || containerY >= height)) {
            cursorX = containerX;
            cursorY = containerY;
        }
        return ret;
    }

    @Override
    public InputResult onMouseDrag(int containerX, int containerY, int mouseButton, double deltaX, double deltaY) {
        beenClicked = true;
        InputResult ret = super.onMouseDrag(containerX, containerY, mouseButton, deltaX, deltaY);
        if (isTransparent) return ret;
        if (!(containerX < 0 || containerY < 0 || containerX >= width || containerY >= height)) {
            cursorX = containerX;
            cursorY = containerY;
        }

        return ret;
    }

    public void pickAtCursor() {
        isTransparent = pickColor(cursorX, cursorY);
    }
}
