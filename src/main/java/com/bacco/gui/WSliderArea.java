package com.bacco.gui;

import com.bacco.libgui.widget.WLabel;
import com.bacco.libgui.widget.WPlainPanel;
import com.bacco.libgui.widget.WSlider;
import com.bacco.libgui.widget.data.CottonAxis;
import net.minecraft.network.chat.Component;

public class WSliderArea extends WPlainPanel {
    WLabel rLabel = new WLabel(Component.translatable("ui.mcrgb_forge.r_for_red"), 0xFF0000);
    WLabel gLabel = new WLabel(Component.translatable("ui.mcrgb_forge.g_for_green"), 0x00FF00);
    WLabel bLabel = new WLabel(Component.translatable("ui.mcrgb_forge.b_for_blue"), 0x0000FF);
    WSlider rSlider = new WSlider(0, 255, CottonAxis.VERTICAL);
    WSlider gSlider = new WSlider(0, 255, CottonAxis.VERTICAL);
    WSlider bSlider = new WSlider(0, 255, CottonAxis.VERTICAL);
}
