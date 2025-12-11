package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.mcrgb_forge.client.analysis.IItemBlockColorSaver;
import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.libgui_forge.client.BackgroundPainter;
import io.github.robak132.libgui_forge.widget.WBox;
import io.github.robak132.libgui_forge.widget.data.Insets;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class WBlockInfoBox extends WBox {

    int lineCount = 0;

    public WBlockInfoBox(Direction.Plane axis, IItemBlockColorSaver item, AbstractGuiScreen gui) {
        super(axis);
        setInsets(Insets.ROOT_PANEL);
        for (int i = 0; i < item.mcrgb_forge$getLength(); i++) {
            List<String> strings = item.mcrgb_forge$getSpriteDetails(i).getStrings();
            List<Integer> colors = item.mcrgb_forge$getSpriteDetails(i).getTextColors();
            if (!strings.isEmpty()) {
                for (int j = 0; j < strings.size(); j++) {
                    var text = Component.literal(strings.get(j));
                    MutableComponent text2 = (MutableComponent) Component.literal("⬛").toFlatList(Style.EMPTY.withColor(colors.get(j))).get(0);
                    if (j > 0) {
                        text2.append(text.toFlatList(Style.EMPTY.withColor(0x707070)).get(0));
                    } else {
                        text2 = (MutableComponent) text.toFlatList(Style.EMPTY.withColor(0x444444)).get(0);
                    }
                    Font textRenderer = Minecraft.getInstance().font;
                    int width = textRenderer.width(text2);
                    WClickableLabel newLabel = new WClickableLabel(text2, new ColorVector(colors.get(j)), gui);
                    newLabel.hoveredProperty();
                    add(newLabel, width, 1);
                    lineCount++;
                }
            }
        }

        setSize(10, this.getWidth());
    }

    /**
     * Constructs a box.
     *
     * @throws NullPointerException if the axis is null
     */

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        setBackgroundPainter(BackgroundPainter.VANILLA);
        super.paint(context, x, y, mouseX, mouseY);
    }
}