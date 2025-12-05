package com.bacco.gui;

import com.bacco.MCRGBClient;
import com.bacco.ColourVector;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class WClickableLabel extends WLabel {
    ColourVector colour;
    MCRGBBaseGui gui;
    Minecraft client;
    MCRGBClient mcrgbClient;
    Component textUnhovered = text;
    MutableComponent textHovered = Component.empty();

    public WClickableLabel(Component text, ColourVector colour, MCRGBBaseGui gui) {
        super(text);
        this.colour = colour;
        this.client = gui.client;
        this.mcrgbClient = gui.mcrgb_forgeClient;
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
        gui.setColour(colour);
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


