package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;
import static io.github.robak132.mcrgb_forge.client.utils.ChatUtils.displayClientLocalisedMessage;
import static io.github.robak132.mcrgb_forge.client.utils.TypeConversionUtils.hexToInt;
import static io.github.robak132.mcrgb_forge.client.utils.TypeConversionUtils.stringToInt;

import io.github.robak132.libgui_forge.widget.WButton;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WLabel;
import io.github.robak132.libgui_forge.widget.WPlainPanel;
import io.github.robak132.libgui_forge.widget.WSlider;
import io.github.robak132.libgui_forge.widget.WToggleButton;
import io.github.robak132.libgui_forge.widget.data.HorizontalAlignment;
import io.github.robak132.libgui_forge.widget.data.Insets;
import io.github.robak132.libgui_forge.widget.data.Texture;
import io.github.robak132.libgui_forge.widget.icon.TextureIcon;
import io.github.robak132.mcrgb_forge.client.MCRGBClient;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteColor;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WButtonWithTooltip;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WColorGuiSlot;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WColorScrollBar;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WColorWheel;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WGradientSlider;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSearchField;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSmartTextField;
import io.github.robak132.mcrgb_forge.client.integration.ClothConfigIntegration;
import io.github.robak132.mcrgb_forge.colors.Color;
import io.github.robak132.mcrgb_forge.colors.Color.ColorModel;
import io.github.robak132.mcrgb_forge.colors.OkLAB;
import io.github.robak132.mcrgb_forge.colors.RGB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

@Slf4j
public class ColorsGuiDescription extends AbstractGuiDescription {

    private final List<ItemStack> stacks = new ArrayList<>();
    private final List<WColorGuiSlot> wColorGuiSlots = new ArrayList<>();
    private final Map<Block, List<SpriteDetails>> blockSpriteMap;
    private final WLabel rLabel = new WLabel(Component.translatable("ui.mcrgb_forge.r_for_red"), 0xFFFF0000);
    private final WSlider rSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    private final WSmartTextField rInput = new WSmartTextField(Component.empty());
    private final WLabel gLabel = new WLabel(Component.translatable("ui.mcrgb_forge.g_for_green"), 0xFF00FF00);
    private final WSlider gSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    private final WSmartTextField gInput = new WSmartTextField(Component.empty());
    private final WLabel bLabel = new WLabel(Component.translatable("ui.mcrgb_forge.b_for_blue"), 0xFF0000FF);
    private final WSlider bSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    private final WSmartTextField bInput = new WSmartTextField(Component.empty());
    private final WButton rgbButton = new WButton(Component.translatable("ui.mcrgb_forge.rgb"));
    private final WButton hsvButton = new WButton(Component.translatable("ui.mcrgb_forge.hsv"));
    private final WButton hslButton = new WButton(Component.translatable("ui.mcrgb_forge.hsl"));
    private final ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
    private final ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
    private final ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
    private final ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
    private final ItemStack horse = new ItemStack(Items.LEATHER_HORSE_ARMOR);
    private final WColorWheel colorWheel = new WColorWheel(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wheel.png"), 0, 0, 1, 1, this);
    private final WToggleButton colorWheelToggle = new WToggleButton();
    private final WGradientSlider wheelValueSlider = new WGradientSlider(0, 255, Direction.Plane.VERTICAL);
    private final WSearchField searchField = new WSearchField(Component.translatable("ui.mcrgb_forge.refine"));
    private final WPlainPanel sliderArea = new WPlainPanel();
    private final WPlainPanel inputs = new WPlainPanel();
    private final WGridPanel armourSlots = new WGridPanel();
    private final WColorScrollBar scrollBar = new WColorScrollBar(this::placeSlots);

    public ColorsGuiDescription(RGB launchColor, Map<Block, List<SpriteDetails>> blockSpriteMap) {
        this.blockSpriteMap = blockSpriteMap;

        WButtonWithTooltip refreshButton = new WButtonWithTooltip(new TextureIcon(ResourceLocation.fromNamespaceAndPath(MOD_ID, "refresh.png")),
                Component.translatable("ui.mcrgb_forge.refresh_info"));
        WButton settingsButton = new WButton(new TextureIcon(ResourceLocation.fromNamespaceAndPath(MOD_ID, "settings.png")));
        colorSort();
        setRootPanel(root);
        root.add(mainPanel, 0, 0);
        mainPanel.setSize(320, 220);
        mainPanel.setInsets(Insets.ROOT_PANEL);
        mainPanel.add(hexInput, 11, 1, 5, 1);
        mainPanel.add(colorDisplay, 16, 1, 2, 2);
        colorDisplay.setLocation(colorDisplay.getAbsoluteX() + 1, colorDisplay.getAbsoluteY() - 1);
        mainPanel.add(scrollBar, 9, 1, 1, SLOTS_HEIGHT - 1);
        mainPanel.add(refreshButton, 17, 11, 1, 1);
        refreshButton.setSize(20, 20);
        refreshButton.setIconSize(18);
        refreshButton.setAlignment(HorizontalAlignment.LEFT);
        refreshButton.setOnClick(() -> {
            Minecraft.getInstance().setScreen(null);
            MCRGBClient.triggerScan();
        });

        mainPanel.add(searchField, 6, 0, 4, 1);
        searchField.setSize(4 * 18, 11);

        mainPanel.add(settingsButton, 17, 0, 1, 1);
        settingsButton.setSize(20, 20);
        settingsButton.setIconSize(18);
        settingsButton.setAlignment(HorizontalAlignment.LEFT);

        mainPanel.add(rgbButton, 10, 11, 1, 1);
        rgbButton.setLocation(201, 205);
        rgbButton.setSize(26, 20);
        rgbButton.setEnabled(false);
        rgbButton.setAlignment(HorizontalAlignment.CENTER);
        mainPanel.add(hsvButton, 13, 11, 1, 1);
        hsvButton.setLocation(237, 205);
        hsvButton.setSize(26, 20);
        hsvButton.setAlignment(HorizontalAlignment.CENTER);
        mainPanel.add(hslButton, 15, 11, 1, 1);
        hslButton.setLocation(273, 205);
        hslButton.setSize(26, 20);
        hslButton.setAlignment(HorizontalAlignment.CENTER);

        mainPanel.add(new WLabel(Component.translatable("ui.mcrgb_forge.header")), 0, 0, 2, 1);
        mainPanel.add(savedPalettesArea, 0, SLOTS_HEIGHT);
        mainPanel.add(sliderArea, 11, 2, 6, 7);

        mainPanel.add(rLabel, 6, 7, 1, 1);
        mainPanel.add(gLabel, 6, 7, 1, 1);
        mainPanel.add(bLabel, 6, 7, 1, 1);
        rLabel.setLocation(211, 50);
        gLabel.setLocation(247, 50);
        bLabel.setLocation(283, 50);

        sliderArea.add(rSlider, 0, 18, 18, 108);
        sliderArea.add(gSlider, 36, 18, 18, 108);
        sliderArea.add(bSlider, 72, 18, 18, 108);

        mainPanel.add(inputs, 10, 9, 2, 1);
        inputs.add(rInput, 14, 9, 26, 1);
        inputs.add(gInput, 50, 9, 26, 1);
        inputs.add(bInput, 86, 9, 26, 1);

        rSlider.setValueChangeListener(this::onSliderValueChange);
        gSlider.setValueChangeListener(this::onSliderValueChange);
        bSlider.setValueChangeListener(this::onSliderValueChange);

        rSlider.setDraggingFinishedListener(v -> colorSort());
        gSlider.setDraggingFinishedListener(v -> colorSort());
        bSlider.setDraggingFinishedListener(v -> colorSort());

        wheelValueSlider.setValueChangeListener((int value) -> {
            colorWheel.setOpaqueTint(new RGB(255, value, value, value).argb());
            colorWheel.pickAtCursor();
        });

        rInput.setCommitListener(this::onValueEntered);
        gInput.setCommitListener(this::onValueEntered);
        bInput.setCommitListener(this::onValueEntered);
        hexInput.setCommitListener(this::onHexEntered);
        searchField.setChangedListener((v) -> colorSort());

        rgbButton.setOnClick(() -> setColor(ColorModel.RGB));
        hsvButton.setOnClick(() -> setColor(ColorModel.HSV));
        hslButton.setOnClick(() -> setColor(ColorModel.HSL));

        colorWheelToggle.setOnToggle(this::toggleColorWheel);

        if (ModList.get().isLoaded("cloth_config")) {
            settingsButton.setOnClick(() -> Minecraft.getInstance().setScreen(ClothConfigIntegration.getConfigScreen(Minecraft.getInstance().screen)));
        } else {
            settingsButton.setOnClick(() -> displayClientLocalisedMessage("warning.mcrgb_forge.noclothconfig"));
        }
        mainPanel.add(armourSlots, 17, 3);

        armourSlots.add(new WColorGuiSlot(helmet, this), 0, 0);
        armourSlots.add(new WColorGuiSlot(chestplate, this), 0, 1);
        armourSlots.add(new WColorGuiSlot(leggings, this), 0, 2);
        armourSlots.add(new WColorGuiSlot(boots, this), 0, 3);
        armourSlots.add(new WColorGuiSlot(horse, this), 0, 4);

        colorWheelToggle.setOffImage(new Texture(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wheel_small.png")));
        colorWheelToggle.setOnImage(new Texture(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sliders.png")));
        mainPanel.add(colorWheelToggle, 17, 10);
        colorWheelToggle.setLocation(314, 180);

        mainPanel.validate(this);
        root.validate(this);
        setColor(launchColor);
        updateArmour();
    }

    @Override
    public void setColor(Color color, ColorModel model) {
        super.setColor(color, model);
        lockWidgets(() -> {
            switch (this.activeColorModel) {
                case RGB:
                    rLabel.setText(Component.translatable("ui.mcrgb_forge.r_for_red"));
                    rLabel.setColor(0xFFFF0000);
                    gLabel.setText(Component.translatable("ui.mcrgb_forge.g_for_green"));
                    gLabel.setColor(0xFF00FF00);
                    bLabel.setText(Component.translatable("ui.mcrgb_forge.b_for_blue"));
                    bLabel.setColor(0xFF0000FF);
                    rSlider.setMinValue(0);
                    gSlider.setMinValue(0);
                    bSlider.setMinValue(0);
                    rSlider.setMaxValue(255);
                    gSlider.setMaxValue(255);
                    bSlider.setMaxValue(255);
                    rgbButton.setEnabled(false);
                    hsvButton.setEnabled(true);
                    hslButton.setEnabled(true);
                    break;
                case HSV:
                    rLabel.setText(Component.translatable("ui.mcrgb_forge.h_for_hue_hsv"));
                    rLabel.setColor(0xFF3F3F3F);
                    gLabel.setText(Component.translatable("ui.mcrgb_forge.s_for_sat_hsv"));
                    gLabel.setColor(0xFF3F3F3F);
                    bLabel.setText(Component.translatable("ui.mcrgb_forge.v_for_val_hsv"));
                    bLabel.setColor(0xFF3F3F3F);
                    rSlider.setMinValue(0);
                    gSlider.setMinValue(0);
                    bSlider.setMinValue(0);
                    rSlider.setMaxValue(360);
                    gSlider.setMaxValue(100);
                    bSlider.setMaxValue(100);
                    rgbButton.setEnabled(true);
                    hsvButton.setEnabled(false);
                    hslButton.setEnabled(true);
                    break;
                case HSL:
                    rLabel.setText(Component.translatable("ui.mcrgb_forge.h_for_hue_hsl"));
                    rLabel.setColor(0xFF3F3F3F);
                    gLabel.setText(Component.translatable("ui.mcrgb_forge.s_for_sat_hsl"));
                    gLabel.setColor(0xFF3F3F3F);
                    bLabel.setText(Component.translatable("ui.mcrgb_forge.l_for_lit_hsl"));
                    bLabel.setColor(0xFF3F3F3F);
                    rSlider.setMinValue(0);
                    gSlider.setMinValue(0);
                    bSlider.setMinValue(0);
                    rSlider.setMaxValue(360);
                    gSlider.setMaxValue(100);
                    bSlider.setMaxValue(100);
                    rgbButton.setEnabled(true);
                    hsvButton.setEnabled(true);
                    hslButton.setEnabled(false);
                    break;
            }
            scrollBar.setValue(0);
            refreshComponents();
        }, (e) -> log.error("Error refreshing color components", e));
    }

    private void onSliderValueChange(int value) {
        setColor(Color.create(activeColorModel, rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
    }

    private void onValueEntered(String value) {
        Integer rInputValue = stringToInt(rInput.getText());
        Integer gInputValue = stringToInt(gInput.getText());
        Integer bInputValue = stringToInt(bInput.getText());

        if (rInputValue != null && gInputValue != null && bInputValue != null) {
            switch (activeColorModel) {
                case RGB -> {
                    rInputValue = Mth.clamp(rInputValue, 0, 255);
                    gInputValue = Mth.clamp(gInputValue, 0, 255);
                    bInputValue = Mth.clamp(bInputValue, 0, 255);
                }
                case HSV, HSL -> {
                    rInputValue = Mth.clamp(rInputValue, 0, 360);
                    gInputValue = Mth.clamp(gInputValue, 0, 100);
                    bInputValue = Mth.clamp(bInputValue, 0, 100);
                }
            }
            setColor(Color.create(activeColorModel, rInputValue, gInputValue, bInputValue));
        }
    }

    private void onHexEntered(String value) {
        Integer valueInt = hexToInt(value);
        if (valueInt != null) setColor(new RGB(valueInt));
    }

    private void refreshComponents() {
        if (!rSlider.isDragging()) rSlider.setValue(activeColor.ch0().intValue());
        if (!gSlider.isDragging()) gSlider.setValue(activeColor.ch1().intValue());
        if (!bSlider.isDragging()) bSlider.setValue(activeColor.ch2().intValue());

        if (!rInput.isFocused()) rInput.setText(String.valueOf(activeColor.ch0()));
        if (!gInput.isFocused()) gInput.setText(String.valueOf(activeColor.ch1()));
        if (!bInput.isFocused()) bInput.setText(String.valueOf(activeColor.ch2()));

        if (!scrollBar.isFocused()) scrollBar.setValue(0);
        if (!hexInput.isFocused()) hexInput.setText(activeColor.toHexString());

        updateArmour();
        colorSort();
        placeSlots();

        if (colorWheelToggle.getToggle()) {
                RGB rgb = activeColor.toRGB();
                int val = Math.max(Math.max(rgb.red(), rgb.green()), rgb.blue());
                colorWheel.setOpaqueTint(new RGB(val, val, val).argb());
            }
    }

    private void updateArmour() {
        final String DISPLAY = "display";
        final String COLOR = "color";

        int hexInt = activeColor.argb();
        helmet.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        chestplate.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        leggings.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        boots.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        horse.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        colorDisplay.setOpaqueTint(hexInt);
    }

    private void colorSort() {
        stacks.clear();
        Map<Block, Double> blockScores = new HashMap<>();
        ForgeRegistries.BLOCKS.forEach(block -> {
            List<SpriteDetails> sprites = blockSpriteMap.get(block);
            if (sprites == null || sprites.isEmpty()) return;

            double score = scoreBlock(activeColor, sprites);
            blockScores.put(block, score);

            if (block.getName().getString().toUpperCase().contains(searchField.getText().toUpperCase())) stacks.add(new ItemStack(block));

        });
        stacks.sort((a, b) -> {
            Block blA = Block.byItem(a.getItem());
            Block blB = Block.byItem(b.getItem());

            double dA = blockScores.getOrDefault(blA, Double.MAX_VALUE);
            double dB = blockScores.getOrDefault(blB, Double.MAX_VALUE);

            return Double.compare(dA, dB);
        });
        scrollBar.setMaxValue(stacks.size() / SLOTS_WIDTH + SLOTS_WIDTH);
        placeSlots();
    }

    private void placeSlots() {
        wColorGuiSlots.forEach(mainPanel::remove);
        int index = SLOTS_WIDTH * scrollBar.getValue();
        for (int j = 1; j < SLOTS_HEIGHT; j++) {
            for (int i = 0; i < SLOTS_WIDTH; i++) {
                if (index >= stacks.size()) break;
                WColorGuiSlot colorGuiSlot = new WColorGuiSlot(stacks.get(index), this);

                if (wColorGuiSlots.size() <= index) {
                    wColorGuiSlots.add(colorGuiSlot);
                } else {
                    wColorGuiSlots.set(index, colorGuiSlot);
                }
                mainPanel.add(colorGuiSlot, i, j);
                index++;

            }
        }
        mainPanel.validate(this);
    }

    private void toggleColorWheel(boolean isToggled) {
        if (isToggled) {
            mainPanel.remove(sliderArea);

            //Remove and re-add inputs to workaround visual bug in 1.20.1 only
            mainPanel.remove(rLabel);
            mainPanel.remove(gLabel);
            mainPanel.remove(bLabel);
            mainPanel.add(rLabel, 1, 1, 1, 1);
            mainPanel.add(gLabel, 1, 1, 1, 1);
            mainPanel.add(bLabel, 1, 1, 1, 1);
            mainPanel.remove(inputs);
            mainPanel.add(inputs, 10, 9, 2, 1);

            mainPanel.remove(armourSlots);
            mainPanel.add(colorWheel, 11, 2, 6, 6);
            mainPanel.add(wheelValueSlider, 17, 2, 1, 6);
            wheelValueSlider.setValue(wheelValueSlider.getMaxValue());
            colorWheel.setLocation(198, 47);
            wheelValueSlider.setLocation(314, 47);
            wheelValueSlider.setSize(18, 128);
            rLabel.setLocation(211, 165);
            gLabel.setLocation(247, 165);
            bLabel.setLocation(283, 165);
        } else {
            mainPanel.add(sliderArea, 11, 2, 6, 7);
            mainPanel.add(armourSlots, 17, 3);
            mainPanel.remove(colorWheel);
            mainPanel.remove(wheelValueSlider);
            rLabel.setLocation(211, 50);
            gLabel.setLocation(247, 50);
            bLabel.setLocation(283, 50);

        }
        root.validate(this);
    }

    private double scoreBlock(Color query, List<SpriteDetails> sprites) {
        OkLAB queryOkLAB = query.toOkLAB();
        double score = 0.0;
        double totalWeight = 0.0;

        for (SpriteDetails sprite : sprites) {
            for (SpriteColor sc : sprite.getColors()) {
                float w = sc.weight() / 100f;
                if (w <= 0.0001f) continue;
                double d = queryOkLAB.distanceWeighted(sc.color().toOkLAB());
                score += d * w;
                totalWeight += w;
            }
        }

        if (totalWeight == 0.0) {
            return Double.MAX_VALUE;
        }

        return score / totalWeight;
    }
}
