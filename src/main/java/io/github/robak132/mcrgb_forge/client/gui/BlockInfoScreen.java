package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;
import static io.github.robak132.mcrgb_forge.client.MCRGBClient.getSprites;

import io.github.robak132.libgui_forge.widget.WButton;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WLabel;
import io.github.robak132.libgui_forge.widget.WScrollPanel;
import io.github.robak132.libgui_forge.widget.data.HorizontalAlignment;
import io.github.robak132.libgui_forge.widget.data.Insets;
import io.github.robak132.libgui_forge.widget.icon.TextureIcon;
import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.mcrgb_forge.client.analysis.IItemBlockColorSaver;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WBlockInfoBox;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WButtonWithTooltip;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WPickableTexture;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WTextureThumbnail;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class BlockInfoScreen extends AbstractGuiScreen {

    WLabel label = new WLabel(Component.translatable("ui.mcrgb_forge.header"));
    WBlockInfoBox infoBox;
    WScrollPanel infoScrollPanel;
    WPickableTexture blockTexture;
    WGridPanel textureThumbs = new WGridPanel();
    ArrayList<TextureAtlasSprite> spritesAL = new ArrayList<>();


    public BlockInfoScreen(ItemStack stack, ColorVector launchColor) {
        ResourceLocation backIdentifier = ResourceLocation.fromNamespaceAndPath(MOD_ID, "back.png");
        TextureIcon backIcon = new TextureIcon(backIdentifier);
        WButton backButton = new WButtonWithTooltip(backIcon, Component.translatable("ui.mcrgb_forge.back_info"));

        setRootPanel(root);
        root.add(mainPanel, 0, 0);
        mainPanel.setSize(320, 220);
        mainPanel.setInsets(Insets.ROOT_PANEL);
        mainPanel.add(hexInput, 11, 1, 5, 1);
        hexInput.setChangedListener(this::hexTyped);
        mainPanel.add(colorDisplay, 16, 1, 2, 2);
        colorDisplay.setLocation(colorDisplay.getAbsoluteX() + 1, colorDisplay.getAbsoluteY() - 1);

        mainPanel.add(label, 0, 0, 2, 1);
        label.setText(stack.getHoverName());

        mainPanel.add(backButton, 17, 0, 1, 1);
        backButton.setSize(20, 20);
        backButton.setIconSize(18);
        backButton.setAlignment(HorizontalAlignment.LEFT);

        backButton.setOnClick(() -> Minecraft.getInstance().setScreen(new ColorScreen(new ColorGui(inputColor))));

        infoBox = new WBlockInfoBox(Direction.Plane.VERTICAL, (IItemBlockColorSaver) stack.getItem(), this);
        infoScrollPanel = new WScrollPanel(infoBox);

        mainPanel.add(this.infoScrollPanel, 11, 3, 7, 9);
        mainPanel.add(savedPalettesArea, 0, 7);

        setColor(launchColor);

        BlockItem blockItem = (BlockItem) stack.getItem();
        Set<TextureAtlasSprite> sprites = getSprites(blockItem.getBlock());
        if (sprites.isEmpty()) {
            return;
        }
        spritesAL.addAll(sprites);

        int length = sprites.size();
        for (int i = 0; i < length; i++) {
            WTextureThumbnail thumbnail = new WTextureThumbnail(spritesAL.get(i).atlasLocation(), spritesAL.get(i).getU0(), spritesAL.get(i).getV0(),
                    spritesAL.get(i).getU1(), spritesAL.get(i).getV1(), i, this);
            textureThumbs.add(thumbnail, i % 3, Math.floorDiv(i, 3));
        }

        blockTexture = new WPickableTexture(spritesAL.get(0).atlasLocation(), spritesAL.get(0).getU0(), spritesAL.get(0).getV0(), spritesAL.get(0).getU1(),
                spritesAL.get(0).getV1(), this);

        mainPanel.add(blockTexture, 0, 1, 6, 6);
        mainPanel.add(textureThumbs, 7, 1, 3, 6);

        root.validate(this);
    }

    public void changeSprite(int i) {
        blockTexture.setImage(spritesAL.get(i).atlasLocation());
        blockTexture.setUv(spritesAL.get(i).getU0(), spritesAL.get(i).getV0(), spritesAL.get(i).getU1(), spritesAL.get(i).getV1());
        root.validate(this);
    }

    public void hexTyped(String value) {
        try {
            ColorVector color = new ColorVector(value);
            if (!hexInput.isFocused()) {
                return;
            }
            if (value.equals(inputColor.getHex())) {
                return;
            }

            inputColor = color;
            colorDisplay.setOpaqueTint(inputColor.asInt());
        } catch (Exception e) {
            // Ignored
        }
    }

}
