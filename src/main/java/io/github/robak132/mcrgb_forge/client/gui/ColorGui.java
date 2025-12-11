package io.github.robak132.mcrgb_forge.client.gui;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

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
import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.mcrgb_forge.client.analysis.IItemBlockColorSaver;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteColor;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import io.github.robak132.mcrgb_forge.client.integration.ClothConfigIntegration;
import io.github.robak132.mcrgb_forge.client.MCRGBClient;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WButtonWithTooltip;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WColorScrollBar;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WColorGuiSlot;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WColorWheel;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WGradientSlider;
import io.github.robak132.mcrgb_forge.client.gui.widgets.WSearchField;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class ColorGui extends AbstractGuiScreen {

    private final List<ItemStack> stacks = new ArrayList<>();
    private final List<WColorGuiSlot> wColorGuiSlots = new ArrayList<>();
    boolean enableSliderListeners = true;
    ColorMode mode = ColorMode.RGB;

    WLabel rLabel = new WLabel(Component.translatable("ui.mcrgb_forge.r_for_red"), 0xFFFF0000);
    WSlider rSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    WTextField rInput = new WTextField(Component.literal(Integer.toString(inputColor.getR())));

    WLabel gLabel = new WLabel(Component.translatable("ui.mcrgb_forge.g_for_green"), 0xFF00FF00);
    WSlider gSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    WTextField gInput = new WTextField(Component.literal(Integer.toString(inputColor.getG())));

    WLabel bLabel = new WLabel(Component.translatable("ui.mcrgb_forge.b_for_blue"), 0xFF0000FF);
    WSlider bSlider = new WSlider(0, 255, Direction.Plane.VERTICAL);
    WTextField bInput = new WTextField(Component.literal(Integer.toString(inputColor.getB())));

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

    public ColorGui(ColorVector launchColor) {
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
            MCRGBClient.refreshColors();
            colorSort();
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
        rSlider.setValue(inputColor.getR());
        sliderArea.add(gSlider, 36, 18, 18, 108);
        gSlider.setValue(inputColor.getG());
        sliderArea.add(bSlider, 72, 18, 18, 108);
        bSlider.setValue(inputColor.getB());

        mainPanel.add(inputs, 10, 9, 2, 1);
        inputs.add(rInput, 14, 9, 26, 1);
        inputs.add(gInput, 50, 9, 26, 1);
        inputs.add(bInput, 86, 9, 26, 1);

        rSlider.setValueChangeListener((int value) -> {
            if (enableSliderListeners) {
                sliderAdjust('r', value);
            }
        });
        gSlider.setValueChangeListener((int value) -> {
            if (enableSliderListeners) {
                sliderAdjust('g', value);
            }
        });
        bSlider.setValueChangeListener((int value) -> {
            if (enableSliderListeners) {
                sliderAdjust('b', value);
            }
        });

        rSlider.setDraggingFinishedListener((int value) -> {
            if (!MCRGBConfig.SLIDER_CONSTANT_UPDATE.get()) {
                colorSort();
            }
        });
        gSlider.setDraggingFinishedListener((int value) -> {
            if (!MCRGBConfig.SLIDER_CONSTANT_UPDATE.get()) {
                colorSort();
            }
        });
        bSlider.setDraggingFinishedListener((int value) -> {
            if (!MCRGBConfig.SLIDER_CONSTANT_UPDATE.get()) {
                colorSort();
            }
        });

        wheelValueSlider.setValueChangeListener((int value) -> {
            colorWheel.setOpaqueTint(FastColor.ARGB32.color(255, value, value, value));
            colorWheel.pickAtCursor();
        });

        rInput.setChangedListener((String value) -> RGBTyped('r', value));
        gInput.setChangedListener((String value) -> RGBTyped('g', value));
        bInput.setChangedListener((String value) -> RGBTyped('b', value));

        hexInput.setChangedListener((String value) -> hexTyped(value, false));
        searchField.setChangedListener((String value) -> colorSort());

        rgbButton.setOnClick(() -> setColorMode(ColorMode.RGB));
        hsvButton.setOnClick(() -> setColorMode(ColorMode.HSV));
        hslButton.setOnClick(() -> setColorMode(ColorMode.HSL));

        colorWheelToggle.setOnToggle(this::toggleColorWheel);

        if (ModList.get().isLoaded("cloth_config")) {
            settingsButton.setOnClick(() -> Minecraft.getInstance().setScreen(ClothConfigIntegration.getConfigScreen(Minecraft.getInstance().screen)));
        } else {
            settingsButton.setOnClick(() -> Minecraft.getInstance().player.displayClientMessage(Component.translatable("warning.mcrgb_forge.noclothconfig"), false));
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

        setColor(launchColor);
        mainPanel.validate(this);
        root.validate(this);
    }

    public void setColorMode(ColorMode cm) {
        enableSliderListeners = false;
        mode = cm;
        ColorVector color = new ColorVector(inputColor.getHex());

        switch (mode) {
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
        hexTyped(color.getHex(), true);
        inputColor = color;
        enableSliderListeners = true;
    }

    public void sliderAdjust(char d, int value) {
        switch (mode) {
            case RGB:
                if (d == 'r') {
                    if (inputColor.getR() == value) {
                        return;
                    }
                    inputColor.setR(value);
                    rInput.setText(Integer.toString(inputColor.getR()));
                }
                if (d == 'g') {
                    if (inputColor.getG() == value) {
                        return;
                    }
                    inputColor.setG(value);
                    gInput.setText(Integer.toString(inputColor.getG()));
                }
                if (d == 'b') {
                    if (inputColor.getB() == value) {
                        return;
                    }
                    inputColor.setB(value);
                    bInput.setText(Integer.toString(inputColor.getB()));
                }
                break;
            case HSV:
                inputColor = ColorVector.fromHSV(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                rInput.setText(Integer.toString(rSlider.getValue()));
                gInput.setText(Integer.toString(gSlider.getValue()));
                bInput.setText(Integer.toString(bSlider.getValue()));
                break;
            case HSL:
                inputColor = ColorVector.fromHSL(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                rInput.setText(Integer.toString(rSlider.getValue()));
                gInput.setText(Integer.toString(gSlider.getValue()));
                bInput.setText(Integer.toString(bSlider.getValue()));
                break;
        }
        hexInput.setText(inputColor.getHex());
        updateArmour();
        if (MCRGBConfig.SLIDER_CONSTANT_UPDATE.get()) {
            colorSort();
        }
    }

    public void RGBTyped(char d, String value) {
        switch (mode) {
            case RGB:
                if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) {
                    return;
                }
                if (Integer.parseInt(value) > 255 || Integer.parseInt(value) < 0) {
                    return;
                }

                if (d == 'r') {
                    if (inputColor.getR() == Integer.parseInt(value)) {
                        return;
                    }
                    inputColor.setR(Integer.parseInt(value));
                    rSlider.setValue(inputColor.getR());
                }
                if (d == 'g') {
                    if (inputColor.getG() == Integer.parseInt(value)) {
                        return;
                    }
                    inputColor.setG(Integer.parseInt(value));
                    gSlider.setValue(inputColor.getG());
                }
                if (d == 'b') {
                    if (inputColor.getB() == Integer.parseInt(value)) {
                        return;
                    }
                    inputColor.setB(Integer.parseInt(value));
                    bSlider.setValue(inputColor.getB());
                }
                break;
            case HSV:
                if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) {
                    return;
                }
                if (d == 'r') {
                    if (Integer.parseInt(value) > 360 || Integer.parseInt(value) < 0) {
                        return;
                    }
                } else {
                    if (Integer.parseInt(value) > 100 || Integer.parseInt(value) < 0) {
                        return;
                    }
                }
                if (d == 'r') {
                    if (inputColor.getR() == Integer.parseInt(value)) {
                        return;
                    }
                    rSlider.setValue(Integer.parseInt(value));
                }
                if (d == 'g') {
                    if (inputColor.getG() == Integer.parseInt(value)) {
                        return;
                    }
                    gSlider.setValue(Integer.parseInt(value));
                }
                if (d == 'b') {
                    if (inputColor.getB() == Integer.parseInt(value)) {
                        return;
                    }
                    bSlider.setValue(Integer.parseInt(value));
                }
                inputColor = ColorVector.fromHSV(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                break;
            case HSL:
                if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) {
                    return;
                }
                if (d == 'r') {
                    if (Integer.parseInt(value) > 360 || Integer.parseInt(value) < 0) {
                        return;
                    }
                } else {
                    if (Integer.parseInt(value) > 100 || Integer.parseInt(value) < 0) {
                        return;
                    }
                }

                if (d == 'r') {
                    if (inputColor.getR() == Integer.parseInt(value)) {
                        return;
                    }
                    rSlider.setValue(Integer.parseInt(value));
                }
                if (d == 'g') {
                    if (inputColor.getG() == Integer.parseInt(value)) {
                        return;
                    }
                    gSlider.setValue(Integer.parseInt(value));
                }
                if (d == 'b') {
                    if (inputColor.getB() == Integer.parseInt(value)) {
                        return;
                    }
                    bSlider.setValue(Integer.parseInt(value));
                }
                inputColor = ColorVector.fromHSL(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                break;
        }
        hexInput.setText(inputColor.getHex());
        updateArmour();
        if (colorWheelToggle.getToggle()) {
            int val = Math.max(Math.max(inputColor.getR(), inputColor.getG()), inputColor.getB());
            colorWheel.setOpaqueTint(new ColorVector(val, val, val).asInt());
        }
    }

    public void hexTyped(String value, boolean modeChanged) {
        enableSliderListeners = false;

        ColorVector color = new ColorVector(value);
        if (!modeChanged && !hexInput.isFocused()) {
            enableSliderListeners = true;
            return;
        }
        if (!modeChanged && value.equals(inputColor.getHex())) {
            enableSliderListeners = true;
            return;
        }
        switch (mode) {
            case RGB:
                rSlider.setValue(color.getR());
                rInput.setText(Integer.toString(color.getR()));

                gSlider.setValue(color.getG());
                gInput.setText(Integer.toString(color.getG()));

                bSlider.setValue(color.getB());
                bInput.setText(Integer.toString(color.getB()));
                break;
            case HSV:
                rSlider.setValue(color.getHue());
                rInput.setText(Integer.toString(color.getHue()));

                gSlider.setValue(color.getSatV());
                gInput.setText(Integer.toString(color.getSatV()));

                bSlider.setValue(color.getVal());
                bInput.setText(Integer.toString(color.getVal()));
                break;
            case HSL:
                rSlider.setValue(color.getHue());
                rInput.setText(Integer.toString(color.getHue()));

                gSlider.setValue(color.getSatL());
                gInput.setText(Integer.toString(color.getSatL()));

                bSlider.setValue(color.getLight());
                bInput.setText(Integer.toString(color.getLight()));
                break;
        }
        inputColor = color;
        updateArmour();
        colorSort();
        enableSliderListeners = true;
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
        ColorVector query = inputColor;

        ForgeRegistries.BLOCKS.forEach(block -> {
            IItemBlockColorSaver itemBlockColorSaver = (IItemBlockColorSaver) block.asItem();

            for (int j = 0; j < itemBlockColorSaver.mcrgb_forge$getLength(); j++) {
                double distance = 0;
                double weightless;
                double weight;
                SpriteDetails sprite = itemBlockColorSaver.mcrgb_forge$getSpriteDetails(j);
                for (int i = 0; i < sprite.getColors().size(); i++) {
                    SpriteColor spriteColor = sprite.getColors().get(i);
                    ColorVector color = spriteColor.color();
                    weight = spriteColor.weight();

                    if (color == null) {
                        return;
                    }
                    weightless = query.distanceSquared(color) + 0.000001;
                    if (weightless / Math.pow(weight, 5) < distance || i == 0) {
                        distance = weightless / Math.pow(weight, 5);
                    }
                }

                if (distance < itemBlockColorSaver.mcrgb_forge$getScore() || j == 0) {
                    itemBlockColorSaver.mcrgb_forge$setScore(distance);
                }
            }
            if (block.getName().getString().toUpperCase().contains(searchField.getText().toUpperCase()) && itemBlockColorSaver.mcrgb_forge$getLength() > 0
                    && block.isEnabled(Minecraft.getInstance().level.enabledFeatures())) {
                stacks.add(new ItemStack(block));
            }
        });

        scrollBar.setMaxValue(stacks.size() / SLOTS_WIDTH + SLOTS_WIDTH);
        stacks.sort((is1, is2) -> {
            double x = ((IItemBlockColorSaver) is1.getItem()).mcrgb_forge$getScore();
            double y = ((IItemBlockColorSaver) is2.getItem()).mcrgb_forge$getScore();
            return Double.compare(x, y);
        });
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
    public void setColor(ColorVector color) {
        switch (mode) {
            case RGB:
                rSlider.setValue(color.getR());
                rInput.setText(Integer.toString(color.getR()));

                gSlider.setValue(color.getG());
                gInput.setText(Integer.toString(color.getG()));

                bSlider.setValue(color.getB());
                bInput.setText(Integer.toString(color.getB()));
                break;
            case HSV:
                rSlider.setValue(color.getHue());
                rInput.setText(Integer.toString(color.getHue()));

                gSlider.setValue(color.getSatV());
                gInput.setText(Integer.toString(color.getSatV()));

                bSlider.setValue(color.getVal());
                bInput.setText(Integer.toString(color.getVal()));
                break;
            case HSL:
                rSlider.setValue(color.getHue());
                rInput.setText(Integer.toString(color.getHue()));

                gSlider.setValue(color.getSatL());
                gInput.setText(Integer.toString(color.getSatL()));

                bSlider.setValue(color.getLight());
                bInput.setText(Integer.toString(color.getLight()));
                break;
        }
        inputColor = color;
        hexInput.setText(inputColor.getHex());
        updateArmour();
        colorSort();
        scrollBar.setValue(0);
        placeSlots();
    }

    public void openBlockInfoGui(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ColorScreen(new BlockInfoScreen(stack, inputColor)));
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

    public enum ColorMode {
        RGB, HSV, HSL
    }
}
