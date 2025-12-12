package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.libgui_forge.client.CottonClientScreen;
import io.github.robak132.libgui_forge.widget.WButton;
import io.github.robak132.libgui_forge.widget.WGridPanel;
import io.github.robak132.libgui_forge.widget.WLabel;
import io.github.robak132.libgui_forge.widget.WPlainPanel;
import io.github.robak132.libgui_forge.widget.WSlider;
import io.github.robak132.libgui_forge.widget.WTextField;
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

import io.github.robak132.mcrgb_forge.client.integration.ClothConfigIntegration;
import io.github.robak132.mcrgb_forge.colors.Color;
import io.github.robak132.mcrgb_forge.colors.Color.ColorModel;
import io.github.robak132.mcrgb_forge.colors.HSL;
import io.github.robak132.mcrgb_forge.colors.HSV;
import io.github.robak132.mcrgb_forge.colors.LAB;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;

import io.github.robak132.mcrgb_forge.colors.RGB;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class ColorsGuiDescription extends AbstractGuiDescription {

    private final List<ItemStack> stacks = new ArrayList<>();
    private final List<WColorGuiSlot> wColorGuiSlots = new ArrayList<>();
    private final Map<Block, List<SpriteDetails>> blockSpriteMap;
    private boolean enableSliderListeners = true;
    private ColorModel mode = ColorModel.RGB;

    WLabel rLabel = new WLabel(Component.translatable("ui.mcrgb_forge.r_for_red"), 0xFFFF0000);
    WSlider rSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    WTextField rInput = new WTextField(Component.empty());

    WLabel gLabel = new WLabel(Component.translatable("ui.mcrgb_forge.g_for_green"), 0xFF00FF00);
    WSlider gSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    WTextField gInput = new WTextField(Component.empty());

    WLabel bLabel = new WLabel(Component.translatable("ui.mcrgb_forge.b_for_blue"), 0xFF0000FF);
    WSlider bSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    WTextField bInput = new WTextField(Component.empty());

    WButton rgbButton = new WButton(Component.translatable("ui.mcrgb_forge.rgb"));
    WButton hsvButton = new WButton(Component.translatable("ui.mcrgb_forge.hsv"));
    WButton hslButton = new WButton(Component.translatable("ui.mcrgb_forge.hsl"));

    ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
    ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
    ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
    ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
    ItemStack horse = new ItemStack(Items.LEATHER_HORSE_ARMOR);

    WColorWheel colorWheel = new WColorWheel(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wheel.png"), 0, 0, 1, 1, this);
    WToggleButton colorWheelToggle = new WToggleButton();
    WGradientSlider wheelValueSlider = new WGradientSlider(0, 255, Direction.Plane.VERTICAL);

    WSearchField searchField = new WSearchField(Component.translatable("ui.mcrgb_forge.refine"));
    WPlainPanel sliderArea = new WPlainPanel();
    WColorScrollBar scrollBar = new WColorScrollBar(this::placeSlots);
    WPlainPanel inputs = new WPlainPanel();
    WGridPanel armourSlots = new WGridPanel();

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
        refreshButton.setOnClick(MCRGBClient::triggerScan);

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

        RGB rgb = inputColor.toRGB();
        sliderArea.add(rSlider, 0, 18, 18, 108);
        rSlider.setValue(rgb.red());
        sliderArea.add(gSlider, 36, 18, 18, 108);
        gSlider.setValue(rgb.green());
        sliderArea.add(bSlider, 72, 18, 18, 108);
        bSlider.setValue(rgb.blue());

        mainPanel.add(inputs, 10, 9, 2, 1);
        inputs.add(rInput, 14, 9, 26, 1);
        inputs.add(gInput, 50, 9, 26, 1);
        inputs.add(bInput, 86, 9, 26, 1);

        rSlider.setValueChangeListener(this::onSliderValueChange);
        gSlider.setValueChangeListener(this::onSliderValueChange);
        bSlider.setValueChangeListener(this::onSliderValueChange);

        rSlider.setDraggingFinishedListener(this::onSliderDrag);
        gSlider.setDraggingFinishedListener(this::onSliderDrag);
        bSlider.setDraggingFinishedListener(this::onSliderDrag);

        wheelValueSlider.setValueChangeListener((int value) -> {
            colorWheel.setOpaqueTint(new RGB(255, value, value, value).argb());
            colorWheel.pickAtCursor();
        });

        rInput.setChangedListener(this::onValueEntered);
        gInput.setChangedListener(this::onValueEntered);
        bInput.setChangedListener(this::onValueEntered);

        hexInput.setChangedListener(this::onHexEntered);
        searchField.setChangedListener((String value) -> colorSort());

        rgbButton.setOnClick(() -> setColorMode(ColorModel.RGB));
        hsvButton.setOnClick(() -> setColorMode(ColorModel.HSV));
        hslButton.setOnClick(() -> setColorMode(ColorModel.HSL));

        colorWheelToggle.setOnToggle(this::toggleColorWheel);

        if (ModList.get().isLoaded("cloth_config")) {
            settingsButton.setOnClick(() -> Minecraft.getInstance().setScreen(ClothConfigIntegration.getConfigScreen(Minecraft.getInstance().screen)));
        } else {
            settingsButton.setOnClick(
                    () -> Minecraft.getInstance().player.displayClientMessage(Component.translatable("warning.mcrgb_forge.noclothconfig"), false));
        }
        updateArmour();

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
    }

    private void onSliderDrag(int value) {
        if (!MCRGBConfig.SLIDER_CONSTANT_UPDATE.get()) {
            colorSort();
        }
    }

    public void setColorMode(ColorModel cm) {
        enableSliderListeners = false;
        mode = cm;

        switch (mode) {
            case RGB:
                inputColor = inputColor.toRGB();
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
                inputColor = inputColor.toHSV();
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
        enableSliderListeners = true;
        refreshComponents();
    }

    private void onSliderValueChange(int value) {
        if (!enableSliderListeners) return;
        inputColor = Color.create(mode, rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
        refreshComponents();
    }

    private void onValueEntered(String value) {
        if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) {
            return;
        }

        int rInputValue = Integer.parseInt(rInput.getText());
        int gInputValue = Integer.parseInt(gInput.getText());
        int bInputValue = Integer.parseInt(bInput.getText());

        inputColor = Color.create(mode, rInputValue, gInputValue, bInputValue);
        refreshComponents();
    }

    private void onHexEntered(String value) {
//        enableSliderListeners = false;
//
//        if (!modeChanged && (hexInput.isFocused() || value.equals(inputColor.toHexString()))) {
//            enableSliderListeners = true;
//            return;
//        }
//        switch (mode) {
//            case RGB:
//                rSlider.setValue(color.getR());
//                rInput.setText(Integer.toString(color.getR()));
//
//                gSlider.setValue(color.getG());
//                gInput.setText(Integer.toString(color.getG()));
//
//                bSlider.setValue(color.getB());
//                bInput.setText(Integer.toString(color.getB()));
//                break;
//            case HSV:
//                rSlider.setValue(color.getHue());
//                rInput.setText(Integer.toString(color.getHue()));
//
//                gSlider.setValue(color.getSatV());
//                gInput.setText(Integer.toString(color.getSatV()));
//
//                bSlider.setValue(color.getVal());
//                bInput.setText(Integer.toString(color.getVal()));
//                break;
//            case HSL:
//                rSlider.setValue(color.getHue());
//                rInput.setText(Integer.toString(color.getHue()));
//
//                gSlider.setValue(color.getSatL());
//                gInput.setText(Integer.toString(color.getSatL()));
//
//                bSlider.setValue(color.getLight());
//                bInput.setText(Integer.toString(color.getLight()));
//                break;
//        }
//        inputColor = color;
//        updateArmour();
//        colorSort();
//        enableSliderListeners = true;
    }

    private void refreshComponents() {
        Number[] values = inputColor.values();
        rSlider.setValue(values[1].intValue());
        rInput.setText(Integer.toString(values[1].intValue()));

        gSlider.setValue(values[2].intValue());
        gInput.setText(Integer.toString(values[2].intValue()));

        bSlider.setValue(values[3].intValue());
        bInput.setText(Integer.toString(values[3].intValue()));

        hexInput.setText(inputColor.toHexString());
        updateArmour();
        if (colorWheelToggle.getToggle()) {
            RGB rgb = inputColor.toRGB();
            int val = Math.max(Math.max(rgb.red(), rgb.green()), rgb.blue());
            colorWheel.setOpaqueTint(new RGB(val, val, val).argb());
        }
        if (MCRGBConfig.SLIDER_CONSTANT_UPDATE.get()) {
            colorSort();
        }
    }

    public void updateArmour() {
        final String DISPLAY = "display";
        final String COLOR = "color";

        int hexInt = getColor();
        helmet.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        chestplate.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        leggings.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        boots.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        horse.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        colorDisplay.setOpaqueTint(hexInt);
    }

    public void colorSort() {
        stacks.clear();
        LAB query = inputColor.toLAB();
        Map<Block, Double> blockScores = new HashMap<>();

        // Preload scan results
        Map<Block, List<SpriteDetails>> map = blockSpriteMap;
        if (map == null) return;

        ForgeRegistries.BLOCKS.forEach(block -> {
            List<SpriteDetails> sprites = map.get(block);
            if (sprites == null || sprites.isEmpty()) return;
            double bestScore = Double.MAX_VALUE;
            for (SpriteDetails sprite : sprites) {
                List<SpriteColor> colors = sprite.getColors();
                if (colors == null || colors.isEmpty()) continue;
                for (SpriteColor sc : colors) {
                    LAB cv = sc.color().toLAB();

                    double weight = sc.weight();   // already normalized 0–1
                    if (weight <= 0.00001) continue;

                    double alpha = cv.alpha();     // you can add this field or hardcode to 1.0
                    if (alpha < 0.05) continue;    // throw out near-transparent pixels

                    double dist = LAB.distanceSquared(query, cv);
                    double w = Math.pow(weight * alpha, 1.5);
                    double score = dist / (w + 0.001);
                    if (score < bestScore)
                        bestScore = score;
                }
            }
            blockScores.put(block, bestScore);
            if (block.getName().getString().toUpperCase().contains(searchField.getText().toUpperCase())) {
                stacks.add(new ItemStack(block));
            }
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

    public void placeSlots() {
        wColorGuiSlots.forEach(mainPanel::remove);
        int index = SLOTS_WIDTH * scrollBar.getValue();
        for (int j = 1; j < SLOTS_HEIGHT; j++) {
            for (int i = 0; i < SLOTS_WIDTH; i++) {
                if (index >= stacks.size()) {
                    break;
                }
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

    @Override
    public void setColor(Color color) {
        switch (this.mode) {
            case RGB:
                RGB rgb = color.toRGB();
                rSlider.setValue(rgb.red());
                rInput.setText(Integer.toString(rgb.red()));

                gSlider.setValue(rgb.green());
                gInput.setText(Integer.toString(rgb.green()));

                bSlider.setValue(rgb.blue());
                bInput.setText(Integer.toString(rgb.blue()));
                break;
            case HSV:
                HSV hsv = color.toHSV();
                rSlider.setValue(Math.round(hsv.hue()));
                rInput.setText(String.valueOf(hsv.hue()));

                gSlider.setValue(Math.round(hsv.saturation()));
                gInput.setText(String.valueOf(hsv.saturation()));

                bSlider.setValue(Math.round(hsv.value()));
                bInput.setText(String.valueOf(hsv.value()));
                break;
            case HSL:
                HSL hsl = color.toHSL();
                rSlider.setValue(Math.round(hsl.hue()));
                rInput.setText(String.valueOf(hsl.hue()));

                gSlider.setValue(Math.round(hsl.saturation()));
                gInput.setText(String.valueOf(hsl.saturation()));

                bSlider.setValue(Math.round(hsl.lightness()));
                bInput.setText(String.valueOf(hsl.lightness()));
                break;
        }
        inputColor = color;
        hexInput.setText(inputColor.toHexString());
        updateArmour();
        colorSort();
        scrollBar.setValue(0);
        placeSlots();
    }

    public void openBlockInfoGui(ItemStack stack) {
        Minecraft.getInstance().setScreen(new CottonClientScreen(new BlockGuiDescription(stack, this.inputColor.toRGB())));
    }

    public void toggleColorWheel(boolean isToggled) {
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

}
