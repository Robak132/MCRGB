package io.github.robak132.mcrgb_forge.client.gui.widgets;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.client.BackgroundPainter;
import io.github.robak132.libgui_forge.widget.WPlainPanel;
import io.github.robak132.libgui_forge.widget.data.HorizontalAlignment;
import io.github.robak132.libgui_forge.widget.icon.TextureIcon;
import io.github.robak132.mcrgb_forge.client.analysis.Palette;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiScreen;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WPaletteWidget extends WPlainPanel {

    int slotsWidth = 9;
    ArrayList<WColorPreviewIcon> savedColors = new ArrayList<>();
    Palette palette;
    AbstractGuiScreen cg;
    WSmallButton editButton = new WSmallButton(new TextureIcon(ResourceLocation.fromNamespaceAndPath(MOD_ID, "edit.png")),
            Component.translatable("ui.mcrgb_forge.edit_palette_info"));
    WSmallButton deleteButton = new WSmallButton(new TextureIcon(ResourceLocation.fromNamespaceAndPath(MOD_ID, "delete.png")),
            Component.translatable("ui.mcrgb_forge.delete_palette_info"));

    public void buildPaletteWidget(AbstractGuiScreen cg) {

        this.setBackgroundPainter(BackgroundPainter.createColorful(0xFFFFFF));
        for (int i = 0; i < slotsWidth; i++) {
            savedColors.add(new WColorPreviewIcon(ResourceLocation.fromNamespaceAndPath(MOD_ID, "square.png"), cg));
            savedColors.get(i).setInteractable(false);
            this.add(savedColors.get(i), i * 17, 0, 18, 18);
        }
        this.add(editButton, (int) (8.6f * 18), 0, 10, 10);
        editButton.setSize(10, 10);
        editButton.setIconSize(9);
        editButton.setAlignment(HorizontalAlignment.LEFT);
        editButton.setOnClick(() -> cg.savedPalettesArea.editPalette(this));

        this.add(deleteButton, (int) (8.6f * 18), 9, 1, 1);
        new TextureIcon(ResourceLocation.fromNamespaceAndPath(MOD_ID, "delete.png")).setColor(0xFF_FC5454);
        deleteButton.setSize(10, 10);
        deleteButton.setIconSize(9);
        deleteButton.setAlignment(HorizontalAlignment.LEFT);
        deleteButton.setOnClick(() -> cg.savedPalettesArea.deletePalette(this));
    }

    @Override
    public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
        super.paint(context, x, y, mouseX, mouseY);
        if (cg.savedPalettesArea.editingPalette == this) {
            context.fill(x, y, this.width, this.height, 0xFF00ff00);
        }
    }
}
