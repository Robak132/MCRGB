package io.github.robak132.mcrgb_forge.client.gui.widgets;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.client.ScreenDrawing;
import io.github.robak132.libgui_forge.widget.WSlider;
import io.github.robak132.libgui_forge.widget.data.WidgetDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class WGradientSlider extends WSlider {
    ResourceLocation valueSliderIdentifier = ResourceLocation.fromNamespaceAndPath(MOD_ID, "value_slider.png");

    public WGradientSlider(int min, int max, Direction.Plane axis) {
        super(min, max, axis);
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.texturedRect(context, x + 5, y, 8, 128, valueSliderIdentifier, 0xFFFFFFFF);
        int thumbX;
        int thumbY;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MOD_ID, "circle4.png");

        if (axis == Direction.Plane.VERTICAL) {
            thumbX = width / 2 - THUMB_SIZE / 2;
            thumbY = widgetDirection == WidgetDirection.UP
              ? (height - THUMB_SIZE) + 1 - (int) (coordToValueRatio * (value - min))
              : Math.round(coordToValueRatio * (value - min));
        } else {
            thumbX = widgetDirection == WidgetDirection.LEFT
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
