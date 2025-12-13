package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;
import static io.github.robak132.mcrgb_forge.client.analysis.ColorScanner.getSprites;
import static io.github.robak132.mcrgb_forge.client.utils.TypeConversionUtils.hexToInt;

import io.github.robak132.libgui_forge.client.CottonClientScreen;
import io.github.robak132.libgui_forge.widget.WButton;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WLabel;
import io.github.robak132.libgui_forge.widget.WScrollPanel;
import io.github.robak132.libgui_forge.widget.data.HorizontalAlignment;
import io.github.robak132.libgui_forge.widget.data.Insets;
import io.github.robak132.libgui_forge.widget.icon.TextureIcon;
import io.github.robak132.mcrgb_forge.client.MCRGBClient;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WBlockInfoBox;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WButtonWithTooltip;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WPickableTexture;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WTextureThumbnail;
import io.github.robak132.mcrgb_forge.colors.RGB;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class BlockGuiDescription extends AbstractGuiDescription {

    private WPickableTexture blockTexture;
    private final List<TextureAtlasSprite> sprites;

    public BlockGuiDescription(ItemStack stack, RGB launchColor) {
        ResourceLocation backIdentifier = ResourceLocation.fromNamespaceAndPath(MOD_ID, "back.png");
        TextureIcon backIcon = new TextureIcon(backIdentifier);
        WButton backButton = new WButtonWithTooltip(backIcon, Component.translatable("ui.mcrgb_forge.back_info"));

        setRootPanel(root);
        root.add(mainPanel, 0, 0);
        mainPanel.setSize(320, 220);
        mainPanel.setInsets(Insets.ROOT_PANEL);
        mainPanel.add(hexInput, 11, 1, 5, 1);
        hexInput.setCommitListener(this::onHexEntered);
        mainPanel.add(colorDisplay, 16, 1, 2, 2);
        colorDisplay.setLocation(colorDisplay.getAbsoluteX() + 1, colorDisplay.getAbsoluteY() - 1);

        WLabel label = new WLabel(Component.translatable("ui.mcrgb_forge.header"));
        mainPanel.add(label, 0, 0, 2, 1);
        label.setText(stack.getHoverName());

        mainPanel.add(backButton, 17, 0, 1, 1);
        backButton.setSize(20, 20);
        backButton.setIconSize(18);
        backButton.setAlignment(HorizontalAlignment.LEFT);
        backButton.setOnClick(this::back);

        WBlockInfoBox infoBox = new WBlockInfoBox(Direction.Plane.VERTICAL, stack.getItem(), (color) -> this.setColor(new RGB(color)));
        WScrollPanel infoScrollPanel = new WScrollPanel(infoBox);

        mainPanel.add(infoScrollPanel, 11, 3, 7, 9);
        mainPanel.add(savedPalettesArea, 0, 7);

        setColor(launchColor);

        sprites = getSprites(((BlockItem) stack.getItem()).getBlock()).stream().toList();
        if (sprites.isEmpty()) return;

        for (int i = 0; i < sprites.size(); i++) {
            WTextureThumbnail thumbnail = new WTextureThumbnail(sprites.get(i).atlasLocation(), sprites.get(i).getU0(), sprites.get(i).getV0(),
                    sprites.get(i).getU1(), sprites.get(i).getV1(), i, this::changeSprite);
            new WGridPanel().add(thumbnail, i % 3, Math.floorDiv(i, 3));
        }
        blockTexture = new WPickableTexture(sprites.get(0).atlasLocation(), sprites.get(0).getU0(), sprites.get(0).getV0(), sprites.get(0).getU1(),
                sprites.get(0).getV1(), this);
        mainPanel.add(blockTexture, 0, 1, 6, 6);
        mainPanel.add(new WGridPanel(), 7, 1, 3, 6);
        root.validate(this);
    }

    public void changeSprite(int i) {
        blockTexture.setImage(sprites.get(i).atlasLocation());
        blockTexture.setUv(sprites.get(i).getU0(), sprites.get(i).getV0(), sprites.get(i).getU1(), sprites.get(i).getV1());
        root.validate(this);
    }

    public void onHexEntered(String value) {
        Integer valueInt = hexToInt(value);
        if (valueInt != null) setColor(new RGB(valueInt));
    }

    private void back() {
        Minecraft.getInstance().setScreen(new CottonClientScreen(new ColorsGuiDescription(activeColor.toRGB(), MCRGBClient.lastScan)));
    }
}
