package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.libgui_forge.widget.WTextField;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class WSearchField extends WTextField {
    public WSearchField(Component component) {
        super(component);
    }

    @Override
    public void setSize(int x, int y) {
        this.width = x;
        this.height = y;
    }

    @Override
    protected void renderText(GuiGraphics context, int x, int y, String visibleText) {
        super.renderText(context, x, y - 4, visibleText);
    }

    @Override
    protected void renderCursor(GuiGraphics context, int x, int y, String visibleText) {
        super.renderCursor(context, x, y - 4, visibleText);
    }

    @Override
    protected void renderSelection(GuiGraphics context, int x, int y, String visibleText) {
        super.renderSelection(context, x, y - 4, visibleText);
    }

    @Override
    protected void renderSuggestion(GuiGraphics context, int x, int y) {
        super.renderSuggestion(context, x, y - 4);
    }
}
