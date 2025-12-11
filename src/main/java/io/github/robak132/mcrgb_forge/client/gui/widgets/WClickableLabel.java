package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.libgui_forge.widget.WLabel;
import io.github.robak132.libgui_forge.widget.data.InputResult;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class WClickableLabel extends WLabel {
    ColorVector color;
    AbstractGuiScreen gui;
    Component textUnhovered = text;
    MutableComponent textHovered = Component.empty();

    public WClickableLabel(Component text, ColorVector color, AbstractGuiScreen gui) {
        super(text);
        this.color = color;
        this.gui = gui;

        List<Component> components = text.toFlatList(Style.EMPTY.withItalic(true).withUnderlined(true));
        List<Component> componentsBase = text.toFlatList(Style.EMPTY);
        if (!components.isEmpty()) {
            components.remove(0);
        }
        components.add(0, componentsBase.get(0));
        for (Component component : components) {
            textHovered.append(component);
        }
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        gui.setColor(color);
        return super.onClick(x, y, button);
    }


    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        if (isWithinBounds(mouseX, mouseY)) {
            setText(textHovered);
        } else {
            setText(textUnhovered);
        }

    }
}


