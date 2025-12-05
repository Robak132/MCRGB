package com.bacco.gui;

import com.bacco.libgui.client.CottonClientScreen;
import com.bacco.libgui.GuiDescription;

public class ColourScreen extends CottonClientScreen {

    public ColourScreen(GuiDescription description) {
        super(description);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
