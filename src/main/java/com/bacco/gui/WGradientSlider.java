package com.bacco.gui;

import com.bacco.libgui.client.ScreenDrawing;
import com.bacco.libgui.widget.WSlider;
import com.bacco.libgui.widget.data.CottonAxis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class WGradientSlider extends WSlider {
    ResourceLocation valueSliderIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "value_slider.png");

    public WGradientSlider(int min, int max, CottonAxis axis) {
        super(min, max, axis);
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.texturedRect(context, x + 5, y, 8, 128, valueSliderIdentifier, 0xFFFFFFFF);
        int thumbX;
        int thumbY;
        ResourceLocation texture = new ResourceLocation("mcrgb_forge", "circle4.png");

        if (axis == CottonAxis.VERTICAL) {
            thumbX = width / 2 - THUMB_SIZE / 2;
            thumbY = direction == Direction.UP
              ? (height - THUMB_SIZE) + 1 - (int) (coordToValueRatio * (value - min))
              : Math.round(coordToValueRatio * (value - min));
        } else {
            thumbX = direction == Direction.LEFT
              ? (width - THUMB_SIZE) - (int) (coordToValueRatio * (value - min))
              : Math.round(coordToValueRatio * (value - min));
            thumbY = height / 2 - THUMB_SIZE / 2;
        }
        ScreenDrawing.texturedRect(context, x + thumbX, y + thumbY + 3, THUMB_SIZE, THUMB_SIZE, texture, 0xFFFFFFFF);

    }

    @Override
    protected int getThumbWidth() {
        return 1;
    }

}
