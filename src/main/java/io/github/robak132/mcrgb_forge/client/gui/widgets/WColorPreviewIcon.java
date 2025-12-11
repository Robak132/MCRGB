package io.github.robak132.mcrgb_forge.client.gui.widgets;

import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.libgui_forge.widget.WSprite;
import io.github.robak132.libgui_forge.widget.data.InputResult;
import io.github.robak132.mcrgb_forge.client.gui.AbstractGuiScreen;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;


public class WColorPreviewIcon extends WSprite {

    int color = 0xFFFFFF;
    AbstractGuiScreen gui;
    @Setter
    @Getter
    boolean interactable = true;

    public WColorPreviewIcon(ResourceLocation image, AbstractGuiScreen gui) {
        super(image);
        this.gui = gui;
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        switch (button) {
            case 0:
                if (!interactable) return InputResult.PROCESSED;
                color = gui.getColor();
                setOpaqueTint(color);
                break;
            case 1:
                if (!interactable) return InputResult.PROCESSED;
                color = 0xFFFFFF;
                setOpaqueTint(color);
                break;
            case 2:
                gui.setColor(new ColorVector(color));
                break;
        }
        return InputResult.PROCESSED;
    }

    public void setColor(int color) {
        this.color = color;
        setOpaqueTint(color);
    }

}
