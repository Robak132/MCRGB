package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.libgui_forge.client.BackgroundPainter;
import io.github.robak132.libgui_forge.widget.WBox;
import io.github.robak132.libgui_forge.widget.data.Insets;
import io.github.robak132.mcrgb_forge.client.MCRGBClient;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiDescription;
import io.github.robak132.mcrgb_forge.colors.RGB;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class WBlockInfoBox extends WBox {

    private Consumer<Integer> onClick;
    int lineCount = 0;

    public WBlockInfoBox(Direction.Plane axis, Item item, Consumer<Integer> onClick) {
        super(axis);
        this.onClick = onClick;
        setInsets(Insets.ROOT_PANEL);
        Block block = Block.byItem(item);

        Map<Block, List<SpriteDetails>> scan = MCRGBClient.getLastScan();
        if (scan == null) {
            return;
        }

        List<SpriteDetails> list = scan.get(block);
        if (list == null || list.isEmpty()) {
            return;
        }

        for (SpriteDetails details : list) {
            List<String> strings = details.getStrings();
            List<Integer> colors = details.getTextColors();
            if (strings.isEmpty()) {
                continue;
            }

            for (int j = 0; j < strings.size(); j++) {
                String s = strings.get(j);
                int color = colors.get(j);
                MutableComponent text = Component.literal(s);
                MutableComponent colorBox = (MutableComponent) Component.literal("⬛").toFlatList(Style.EMPTY.withColor(color)).get(0);
                Component out;
                if (j > 0) {
                    // gray text for subsequent lines
                    Component grayText = text.toFlatList(Style.EMPTY.withColor(0x707070)).get(0);
                    out = colorBox.append(grayText);
                } else {
                    // title styled in darker gray
                    out = text.toFlatList(Style.EMPTY.withColor(0x444444)).get(0);
                }
                Font textRenderer = Minecraft.getInstance().font;
                int width = textRenderer.width(out);
                WClickableLabel newLabel = new WClickableLabel(out, () -> onClick.accept(color));
                newLabel.hoveredProperty();
                add(newLabel, width, 1);
                lineCount++;
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