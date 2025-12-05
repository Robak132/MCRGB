package com.bacco.gui;

import com.bacco.libgui.widget.WButton;
import com.bacco.libgui.widget.WLabel;
import com.bacco.libgui.widget.WListPanel;
import com.bacco.libgui.widget.WPlainPanel;
import com.bacco.MCRGBClient;
import com.bacco.Palette;
import com.bacco.libgui.client.BackgroundPainter;
import com.bacco.ColourVector;
import com.bacco.libgui.widget.data.HorizontalAlignment;
import com.bacco.libgui.widget.data.VerticalAlignment;
import com.bacco.libgui.widget.icon.TextureIcon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class WSavedPalettesArea extends WPlainPanel {
    WLabel savedColoursLabel = new WLabel(Component.translatable("ui.mcrgb_forge.saved_colours"));
    ResourceLocation colourIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "square.png");
    List<WColourPreviewIcon> savedColours = new ArrayList<>();
    ResourceLocation savePaletteIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "save.png");
    TextureIcon savePaletteIcon = new TextureIcon(savePaletteIdentifier);
    WButton savePaletteButton = new WButton(savePaletteIcon);

    WPaletteWidget editingPalette = null;
    WListPanel<Palette, WPaletteWidget> paletteList;

    MCRGBBaseGui cg;

    public WSavedPalettesArea(MCRGBBaseGui gui, int slotsWidth, int slotsHeight) {
        this.cg = gui;
        BiConsumer<Palette, WPaletteWidget> configurator = (Palette p, WPaletteWidget pwig) -> {
            pwig.cg = cg;
            pwig.palette = p;
            pwig.buildPaletteWidget(cg);
            for (int i = 0; i < pwig.savedColours.size(); i++) {
                String hex = p.getColour(i).getHex().replace("#", "");
                int c = Integer.parseInt(hex, 16);
                pwig.savedColours.get(i).setColour(c);
            }
        };


        this.add(savedColoursLabel, 0, slotsHeight, 2, 1);
        savedColoursLabel.setVerticalAlignment(VerticalAlignment.BOTTOM);

        for (int i = 0; i < slotsWidth; i++) {
            savedColours.add(new WColourPreviewIcon(colourIdentifier, cg));

            this.add(savedColours.get(i), i * 17, slotsHeight + 5, 18, 18);
        }

        this.add(savePaletteButton, slotsWidth * 18, slotsHeight + 1, 20, 20);
        savePaletteButton.setSize(20, 20);
        savePaletteButton.setIconSize(18);
        savePaletteButton.setAlignment(HorizontalAlignment.LEFT);

        paletteList = new WListPanel<>(MCRGBClient.palettes, WPaletteWidget::new, configurator);

        paletteList.setBackgroundPainter(BackgroundPainter.createColorful(0x999999));
        paletteList.setListItemHeight(19);
        this.add(paletteList, 0, 2, 10, 3);
        paletteList.setLocation(0, 36);
        paletteList.setSize(10 * 18, (int) (2.8f * 18));

        savePaletteButton.setOnClick(this::savePalette);

    }

    Palette createPalette() {
        Palette newPallet = new Palette();
        for (WColourPreviewIcon savedColour : savedColours) {
            newPallet.addColour(new ColourVector(savedColour.colour));
        }
        return newPallet;
    }

    void updatePalette(WPaletteWidget updatingPalette) {
        for (int i = 0; i < savedColours.size(); i++) {
            updatingPalette.savedColours.get(i).setColour(savedColours.get(i).colour);
            updatingPalette.palette.setColour(i, new ColourVector(savedColours.get(i).colour));
        }
    }

    void savePalette() {
        if (editingPalette == null) {
            MCRGBClient.palettes.add(createPalette());
        } else {
            updatePalette(editingPalette);
            editingPalette = null;
        }
        for (WColourPreviewIcon savedColour : savedColours) {
            savedColour.setColour(0xffffffff);
        }
        MCRGBClient.savePalettes();
        cg.mainPanel.validate(cg);
    }

    public void deletePalette(WPaletteWidget pwig) {

        MCRGBClient.palettes.remove(pwig.palette);
        editingPalette = null;
        cg.root.validate(cg);
        MCRGBClient.savePalettes();
    }

    public void editPalette(WPaletteWidget pwig) {
        if (editingPalette == pwig) {
            editingPalette = null;
            for (WColourPreviewIcon savedColour : savedColours) {
                savedColour.setColour(0xffffffff);
            }
            return;
        }
        for (int i = 0; i < savedColours.size(); i++) {
            savedColours.get(i).setColour(pwig.savedColours.get(i).colour);
        }
        editingPalette = pwig;
    }
}
