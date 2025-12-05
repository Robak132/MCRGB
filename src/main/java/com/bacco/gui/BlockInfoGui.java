package com.bacco.gui;

import com.bacco.ColourVector;
import com.bacco.IItemBlockColourSaver;
import com.bacco.MCRGBClient;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BlockInfoGui extends MCRGBBaseGui {
    WLabel label = new WLabel(Component.translatable("ui.mcrgb_forge.header"));
    WBlockInfoBox infoBox;
    WScrollPanel infoScrollPanel;
    WPickableTexture blockTexture;
    WGridPanel textureThumbs = new WGridPanel();
    ArrayList<TextureAtlasSprite> spritesAL = new ArrayList<>();


    public BlockInfoGui(Minecraft client, MCRGBClient mcrgb_forgeClient, ItemStack stack, ColourVector launchColour) {

        this.client = client;
        this.mcrgb_forgeClient = mcrgb_forgeClient;

        ResourceLocation backIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "back.png");
        TextureIcon backIcon = new TextureIcon(backIdentifier);
        savedPalettesArea = new WSavedPalettesArea(this, 9, 7);
        WButton backButton = new WButton(backIcon) {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Component.translatable("ui.mcrgb_forge.back_info"));
                super.addTooltip(tooltip);
            }
        };

        setRootPanel(root);
        root.add(mainPanel, 0, 0);
        mainPanel.setSize(320, 220);
        mainPanel.setInsets(Insets.ROOT_PANEL);
        mainPanel.add(hexInput, 11, 1, 5, 1);
        hexInput.setChangedListener((String value) -> hexTyped(value, false));
        mainPanel.add(colourDisplay, 16, 1, 2, 2);
        colourDisplay.setLocation(colourDisplay.getAbsoluteX() + 1, colourDisplay.getAbsoluteY() - 1);


        mainPanel.add(label, 0, 0, 2, 1);
        label.setText(stack.getHoverName());

        mainPanel.add(backButton, 17, 0, 1, 1);
        backButton.setSize(20, 20);
        backButton.setIconSize(18);
        backButton.setAlignment(HorizontalAlignment.LEFT);

        backButton.setOnClick(() -> client.setScreen(new ColourScreen(new ColourGui(client, inputColour))));

        infoBox = new WBlockInfoBox(CottonAxis.VERTICAL, (IItemBlockColourSaver) stack.getItem(), this);
        infoScrollPanel = new WScrollPanel(infoBox);

        mainPanel.add(this.infoScrollPanel, 11, 3, 7, 9);

        mainPanel.add(savedPalettesArea, 0, 7);

        setColour(launchColour);


        BlockItem bi = (BlockItem) stack.getItem();
        Block block = bi.getBlock();

        Set<TextureAtlasSprite> sprites = new HashSet<>();
        //try to get the default top texture sprite. if fails, report error and skip this block

        block.getStateDefinition().getPossibleStates().forEach(state -> {
            try {
                BakedModel model = client.getModelManager().getBlockModelShaper().getBlockModel(state);
                sprites.add(model.getQuads(state, Direction.UP, RandomSource.create()).get(0).getSprite());
                sprites.add(model.getQuads(state, Direction.DOWN, RandomSource.create()).get(0).getSprite());
                sprites.add(model.getQuads(state, Direction.NORTH, RandomSource.create()).get(0).getSprite());
                sprites.add(model.getQuads(state, Direction.SOUTH, RandomSource.create()).get(0).getSprite());
                sprites.add(model.getQuads(state, Direction.EAST, RandomSource.create()).get(0).getSprite());
                sprites.add(model.getQuads(state, Direction.WEST, RandomSource.create()).get(0).getSprite());
            } catch (Exception e) {
                // Ignored
            }
        });
        if (sprites.isEmpty()) {
            return;
        }

        spritesAL.addAll(sprites);

        int length = sprites.size();
        for (int i = 0; i < length; i++) {
            WTextureThumbnail thumbnail = new WTextureThumbnail(spritesAL.get(i).atlasLocation(), spritesAL.get(i).getU0(), spritesAL.get(i).getV0(), spritesAL.get(i).getU1(), spritesAL.get(i).getV1(), i, this);
            textureThumbs.add(thumbnail, i % 3, Math.floorDiv(i, 3));
        }

        blockTexture = new WPickableTexture(spritesAL.get(0).atlasLocation(), spritesAL.get(0).getU0(), spritesAL.get(0).getV0(), spritesAL.get(0).getU1(), spritesAL.get(0).getV1(), client, this);

        mainPanel.add(blockTexture, 0, 1, 6, 6);
        mainPanel.add(textureThumbs, 7, 1, 3, 6);


        root.validate(this);
    }

    public void changeSprite(int i) {
        blockTexture.setImage(spritesAL.get(i).atlasLocation());
        blockTexture.setUv(spritesAL.get(i).getU0(), spritesAL.get(i).getV0(), spritesAL.get(i).getU1(), spritesAL.get(i).getV1());
        root.validate(this);
    }

    public void hexTyped(String value, boolean modeChanged) {
        try {
            ColourVector colour = new ColourVector(value);
            if (!hexInput.isFocused()) {
                return;
            }
            if (value.equals(inputColour.getHex())) {
                return;
            }

            inputColour = colour;
            colourDisplay.setOpaqueTint(inputColour.asInt());
        } catch (Exception e) {
            // Ignored
        }
    }

}
