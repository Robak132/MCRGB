package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.libgui_forge.widget.WLabel;
import io.github.robak132.libgui_forge.widget.data.InputResult;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiDescription;
import io.github.robak132.mcrgb_forge.colors.RGB;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class WClickableLabel extends WLabel {
    Component textUnhovered = text;
    MutableComponent textHovered = Component.empty();
    Runnable onClick;

    public WClickableLabel(Component text) {
        this(text, null);
    }

    public WClickableLabel(Component text, Runnable onClick) {
        super(text);
        this.onClick = onClick;

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
        if (onClick != null) {
            onClick.run();
        }
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

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }
}


