package com.bacco.gui;

import com.bacco.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class ColourGui extends MCRGBBaseGui {
    static int slotsHeight = 7;
    static int slotsWidth = 9;
    private final ArrayList<ItemStack> stacks = new ArrayList<>();
    private final ArrayList<WColourGuiSlot> wColourGuiSlots = new ArrayList<>();
    ColourGui cg = this;
    WLabel label = new WLabel(Component.translatable("ui.mcrgb_forge.header"));
    WLabel rLabel = new WLabel(Component.translatable("ui.mcrgb_forge.r_for_red"), 0xFFFF0000);
    WScrollBar scrollBar = new WScrollBar(CottonAxis.VERTICAL) {
        @Override
        public InputResult onMouseDrag(int x, int y, int button, double deltaX, double deltaY) {
            placeSlots();
            return super.onMouseDrag(x, y, button, deltaX, deltaY);
        }

        @Override
        public InputResult onMouseScroll(int x, int y, double vAmount) {
            placeSlots();
            setValue(getValue() + (int) -vAmount);
            return InputResult.PROCESSED;
        }

    };
    WLabel gLabel = new WLabel(Component.translatable("ui.mcrgb_forge.g_for_green"), 0xFF00FF00);
    WLabel bLabel = new WLabel(Component.translatable("ui.mcrgb_forge.b_for_blue"), 0xFF0000FF);
    WSlider rSlider = new WSlider(0, 255, CottonAxis.VERTICAL);
    WSlider gSlider = new WSlider(0, 255, CottonAxis.VERTICAL);
    WSlider bSlider = new WSlider(0, 255, CottonAxis.VERTICAL);
    WPlainPanel inputs = new WPlainPanel();
    WTextField rInput = new WTextField(Component.literal(Integer.toString(inputColour.r)));
    WTextField gInput = new WTextField(Component.literal(Integer.toString(inputColour.g)));
    WTextField bInput = new WTextField(Component.literal(Integer.toString(inputColour.b)));
    ResourceLocation refreshIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "refresh.png");
    TextureIcon refreshIcon = new TextureIcon(refreshIdentifier);
    WButton refreshButton = new WButton(refreshIcon) {

        @Override
        public void addTooltip(TooltipBuilder tooltip) {
            tooltip.add(Component.translatable("ui.mcrgb_forge.refresh_info"));
            super.addTooltip(tooltip);
        }
    };
    ResourceLocation settingsIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "settings.png");
    TextureIcon settingsIcon = new TextureIcon(settingsIdentifier);
    WButton settingsButton = new WButton(settingsIcon);
    WButton rgbButton = new WButton(Component.translatable("ui.mcrgb_forge.rgb"));
    WButton hsvButton = new WButton(Component.translatable("ui.mcrgb_forge.hsv"));
    WButton hslButton = new WButton(Component.translatable("ui.mcrgb_forge.hsl"));
    ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
    ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
    ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
    ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
    ItemStack horse = new ItemStack(Items.LEATHER_HORSE_ARMOR);
    WGridPanel armourSlots = new WGridPanel();
    WColourGuiSlot helmSlot = new WColourGuiSlot(helmet, cg);
    WColourGuiSlot chestSlot = new WColourGuiSlot(chestplate, cg);
    WColourGuiSlot legsSlot = new WColourGuiSlot(leggings, cg);
    WColourGuiSlot bootSlot = new WColourGuiSlot(boots, cg);
    WColourGuiSlot horseSlot = new WColourGuiSlot(horse, cg);
    WToggleButton colourWheelToggle = new WToggleButton();
    WPlainPanel sliderArea = new WSliderArea();
    ResourceLocation wheelIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "wheel.png");
    WColourWheel colourWheel;
    ResourceLocation wheelIconIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "wheel_small.png");
    Texture wheelTex = new Texture(wheelIconIdentifier);
    WGradientSlider wheelValueSlider = new WGradientSlider(0, 255, CottonAxis.VERTICAL);
    ResourceLocation sliderIconIdentifier = ResourceLocation.tryBuild("mcrgb_forge", "sliders.png");
    Texture sliderTex = new Texture(sliderIconIdentifier);
    WTextField searchField = new WTextField(Component.translatable("ui.mcrgb_forge.refine")) {

        @Override
        public void setSize(int x, int y) {
            this.width = x;
            this.height = y;
        }

        @Override

        protected void renderText(GuiGraphics context, int x, int y, String visibleText) {
            super.renderText(context, x, y - 4, visibleText);
        }

        @Override
        protected void renderCursor(GuiGraphics context, int x, int y, String visibleText) {
            super.renderCursor(context, x, y - 4, visibleText);
        }

        @Override
        protected void renderSelection(GuiGraphics context, int x, int y, String visibleText) {
            super.renderSelection(context, x, y - 4, visibleText);
        }

        @Override

        protected void renderSuggestion(GuiGraphics context, int x, int y) {
            super.renderSuggestion(context, x, y - 4);
        }

    };
    boolean enableSliderListeners = true;
    ColourMode mode = ColourMode.RGB;
    public ColourGui(Minecraft client, ColourVector launchColour) {
        this.client = client;
        colourWheel = new WColourWheel(wheelIdentifier, 0, 0, 1, 1, client, this);
        savedPalettesArea = new WSavedPalettesArea(this, slotsWidth, slotsHeight);
        colourSort();
        setRootPanel(root);
        root.add(mainPanel, 0, 0);
        mainPanel.setSize(320, 220);
        mainPanel.setInsets(Insets.ROOT_PANEL);
        mainPanel.add(hexInput, 11, 1, 5, 1);
        mainPanel.add(colourDisplay, 16, 1, 2, 2);
        colourDisplay.setLocation(colourDisplay.getAbsoluteX() + 1, colourDisplay.getAbsoluteY() - 1);
        mainPanel.add(scrollBar, 9, 1, 1, slotsHeight - 1);
        mainPanel.add(refreshButton, 17, 11, 1, 1);
        refreshButton.setSize(20, 20);
        refreshButton.setIconSize(18);
        refreshButton.setAlignment(HorizontalAlignment.LEFT);
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

        mainPanel.add(label, 0, 0, 2, 1);
        mainPanel.add(savedPalettesArea, 0, slotsHeight);

        mainPanel.add(sliderArea, 11, 2, 6, 7);

        mainPanel.add(rLabel, 6, 7, 1, 1);
        mainPanel.add(gLabel, 6, 7, 1, 1);
        mainPanel.add(bLabel, 6, 7, 1, 1);
        rLabel.setLocation(211, 50);
        gLabel.setLocation(247, 50);
        bLabel.setLocation(283, 50);

        sliderArea.add(rSlider, 0, 18, 18, 108);
        rSlider.setValue(inputColour.r);
        sliderArea.add(gSlider, 36, 18, 18, 108);
        gSlider.setValue(inputColour.g);
        sliderArea.add(bSlider, 72, 18, 18, 108);
        bSlider.setValue(inputColour.b);

        mainPanel.add(inputs, 10, 9, 2, 1);
        inputs.add(rInput, 14, 9, 26, 1);
        inputs.add(gInput, 50, 9, 26, 1);
        inputs.add(bInput, 86, 9, 26, 1);

        rSlider.setValueChangeListener((int value) -> {
            if (enableSliderListeners) sliderAdjust('r', value);
        });
        gSlider.setValueChangeListener((int value) -> {
            if (enableSliderListeners) sliderAdjust('g', value);
        });
        bSlider.setValueChangeListener((int value) -> {
            if (enableSliderListeners) sliderAdjust('b', value);
        });

        rSlider.setDraggingFinishedListener((int value) -> {
            if (!MCRGBConfig.instance.sliderConstantUpdate) colourSort();
        });
        gSlider.setDraggingFinishedListener((int value) -> {
            if (!MCRGBConfig.instance.sliderConstantUpdate) colourSort();
        });
        bSlider.setDraggingFinishedListener((int value) -> {
            if (!MCRGBConfig.instance.sliderConstantUpdate) colourSort();
        });

        wheelValueSlider.setValueChangeListener((int value) -> {
            colourWheel.setOpaqueTint(FastColor.ARGB32.color(255, value, value, value));
            colourWheel.pickAtCursor();
        });

        rInput.setChangedListener((String value) -> RGBTyped('r', value));
        gInput.setChangedListener((String value) -> RGBTyped('g', value));
        bInput.setChangedListener((String value) -> RGBTyped('b', value));

        hexInput.setChangedListener((String value) -> hexTyped(value, false));
        searchField.setChangedListener((String value) -> colourSort());

        refreshButton.setOnClick(() -> {
            MCRGBClient.refreshColours();
            colourSort();
        });

        rgbButton.setOnClick(() -> setColourMode(ColourMode.RGB));
        hsvButton.setOnClick(() -> setColourMode(ColourMode.HSV));
        hslButton.setOnClick(() -> setColourMode(ColourMode.HSL));

        colourWheelToggle.setOnToggle(this::ToggleColourWheel);

        if (ModList.get().isLoaded("cloth_config")) {
            settingsButton.setOnClick(() -> Minecraft.getInstance().setScreen(ClothConfigIntegration.getConfigScreen(client.screen)));
        } else {
            settingsButton.setOnClick(() -> client.player.displayClientMessage(Component.translatable("warning.mcrgb_forge.noclothconfig"), false));
        }
        updateArmour();

        mainPanel.add(armourSlots, 17, 3);

        armourSlots.add(helmSlot, 0, 0);
        armourSlots.add(chestSlot, 0, 1);
        armourSlots.add(legsSlot, 0, 2);
        armourSlots.add(bootSlot, 0, 3);
        armourSlots.add(horseSlot, 0, 4);

        colourWheelToggle.setOffImage(wheelTex);
        colourWheelToggle.setOnImage(sliderTex);
        mainPanel.add(colourWheelToggle, 17, 10);
        colourWheelToggle.setLocation(314, 180);

        setColour(launchColour);
        mainPanel.validate(this);
    }

    public void setColourMode(ColourMode cm) {
        enableSliderListeners = false;
        mode = cm;
        ColourVector colour = new ColourVector(inputColour.getHex());

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
        hexTyped(colour.getHex(), true);
        inputColour = colour;
        enableSliderListeners = true;
    }

    public void sliderAdjust(char d, int value) {
        switch (mode) {
            case RGB:
                if (d == 'r') {
                    if (inputColour.r == value) return;
                    inputColour.r = value;
                    rInput.setText(Integer.toString(inputColour.r));
                }
                if (d == 'g') {
                    if (inputColour.g == value) return;
                    inputColour.g = value;
                    gInput.setText(Integer.toString(inputColour.g));
                }
                if (d == 'b') {
                    if (inputColour.b == value) return;
                    inputColour.b = value;
                    bInput.setText(Integer.toString(inputColour.b));
                }
                break;
            case HSV:
                inputColour.fromHSV(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                rInput.setText(Integer.toString(rSlider.getValue()));
                gInput.setText(Integer.toString(gSlider.getValue()));
                bInput.setText(Integer.toString(bSlider.getValue()));
                break;
            case HSL:
                inputColour.fromHSL(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                rInput.setText(Integer.toString(rSlider.getValue()));
                gInput.setText(Integer.toString(gSlider.getValue()));
                bInput.setText(Integer.toString(bSlider.getValue()));
                break;
        }
        hexInput.setText(inputColour.getHex());
        updateArmour();
        if (MCRGBConfig.instance.sliderConstantUpdate) {
            colourSort();
        }
    }

    public void RGBTyped(char d, String value) {
        try {
            switch (mode) {
                case RGB:
                    if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) return;
                    if (Integer.parseInt(value) > 255 || Integer.parseInt(value) < 0) return;

                    if (d == 'r') {
                        if (inputColour.r == Integer.parseInt(value)) return;
                        inputColour.r = Integer.parseInt(value);
                        rSlider.setValue(inputColour.r);
                    }
                    if (d == 'g') {
                        if (inputColour.g == Integer.parseInt(value)) return;
                        inputColour.g = Integer.parseInt(value);
                        gSlider.setValue(inputColour.g);
                    }
                    if (d == 'b') {
                        if (inputColour.b == Integer.parseInt(value)) return;
                        inputColour.b = Integer.parseInt(value);
                        bSlider.setValue(inputColour.b);
                    }
                    break;
                case HSV:
                    if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) return;
                    if (d == 'r') {
                        if (Integer.parseInt(value) > 360 || Integer.parseInt(value) < 0) return;
                    } else {
                        if (Integer.parseInt(value) > 100 || Integer.parseInt(value) < 0) return;
                    }
                    if (d == 'r') {
                        if (inputColour.r == Integer.parseInt(value)) return;
                        rSlider.setValue(Integer.parseInt(value));
                    }
                    if (d == 'g') {
                        if (inputColour.g == Integer.parseInt(value)) return;
                        gSlider.setValue(Integer.parseInt(value));
                    }
                    if (d == 'b') {
                        if (inputColour.b == Integer.parseInt(value)) return;
                        bSlider.setValue(Integer.parseInt(value));
                    }
                    inputColour.fromHSV(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                    break;
                case HSL:
                    if (!rInput.isFocused() && !gInput.isFocused() && !bInput.isFocused()) return;
                    if (d == 'r') {
                        if (Integer.parseInt(value) > 360 || Integer.parseInt(value) < 0) return;
                    } else {
                        if (Integer.parseInt(value) > 100 || Integer.parseInt(value) < 0) return;
                    }

                    if (d == 'r') {
                        if (inputColour.r == Integer.parseInt(value)) return;
                        rSlider.setValue(Integer.parseInt(value));
                    }
                    if (d == 'g') {
                        if (inputColour.g == Integer.parseInt(value)) return;
                        gSlider.setValue(Integer.parseInt(value));
                    }
                    if (d == 'b') {
                        if (inputColour.b == Integer.parseInt(value)) return;
                        bSlider.setValue(Integer.parseInt(value));
                    }
                    inputColour.fromHSL(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
                    break;
            }
            hexInput.setText(inputColour.getHex());
            updateArmour();
            if (colourWheelToggle.getToggle()) {
                int val = Math.max(Math.max(inputColour.r, inputColour.g), inputColour.b);
                colourWheel.setOpaqueTint(new ColourVector(val, val, val).asInt());
            }
        } catch (Exception ignored) {
            // Ignored
        }
    }

    public void hexTyped(String value, boolean modeChanged) {
        enableSliderListeners = false;

        try {
            ColourVector colour = new ColourVector(value);
            if (!modeChanged && !hexInput.isFocused()) {
                enableSliderListeners = true;
                return;
            }
            if (!modeChanged && value.equals(inputColour.getHex())) {
                enableSliderListeners = true;
                return;
            }
            switch (mode) {
                case RGB:
                    rSlider.setValue(colour.r);
                    rInput.setText(Integer.toString(colour.r));

                    gSlider.setValue(colour.g);
                    gInput.setText(Integer.toString(colour.g));

                    bSlider.setValue(colour.b);
                    bInput.setText(Integer.toString(colour.b));
                    break;
                case HSV:
                    rSlider.setValue(colour.getHue());
                    rInput.setText(Integer.toString(colour.getHue()));

                    gSlider.setValue(colour.getSatV());
                    gInput.setText(Integer.toString(colour.getSatV()));

                    bSlider.setValue(colour.getVal());
                    bInput.setText(Integer.toString(colour.getVal()));
                    break;
                case HSL:
                    rSlider.setValue(colour.getHue());
                    rInput.setText(Integer.toString(colour.getHue()));

                    gSlider.setValue(colour.getSatL());
                    gInput.setText(Integer.toString(colour.getSatL()));

                    bSlider.setValue(colour.getLight());
                    bInput.setText(Integer.toString(colour.getLight()));
                    break;
            }
            inputColour = colour;
            updateArmour();
            colourSort();
        } catch (Exception ignored) {
            // Ignored
        }
        enableSliderListeners = true;
    }

    public void updateArmour() {
        final String DISPLAY = "display";
        final String COLOR = "color";

        int hexInt = getColour();
        helmet.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        chestplate.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        leggings.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        boots.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        horse.getOrCreateTagElement(DISPLAY).putInt(COLOR, hexInt);
        colourDisplay.setOpaqueTint(hexInt);
    }

    public void colourSort() {
        stacks.clear();
        ColourVector query = inputColour;

        ForgeRegistries.BLOCKS.forEach(block -> {
            try {
                for (int j = 0; j < ((IItemBlockColourSaver) block.asItem()).getLength(); j++) {
                    double distance = 0;
                    double weightless;
                    double weight;
                    SpriteDetails sprite = ((IItemBlockColourSaver) block.asItem()).getSpriteDetails(j);
                    for (int i = 0; i < sprite.colourinfo.size(); i++) {
                        ColourVector colour = sprite.colourinfo.get(i);
                        if (colour == null) return;
                        weightless = query.distance(colour) + 0.000001;
                        weight = sprite.weights.get(i);
                        if (weightless / Math.pow(weight, 5) < distance || i == 0) {
                            distance = weightless / Math.pow(weight, 5);
                        }
                    }

                    if (distance < ((IItemBlockColourSaver) block.asItem()).getScore() || j == 0) {
                        ((IItemBlockColourSaver) block.asItem()).setScore(distance);
                    }
                }
                if (block.getName().getString().toUpperCase().contains(searchField.getText().toUpperCase()) && block.asItem() != null && ((IItemBlockColourSaver) block.asItem()).getLength() > 0 && block.isEnabled(client.level.enabledFeatures())) {
                    stacks.add(new ItemStack(block));
                }

            } catch (Exception ignored) {
                // Ignored
            }
        });

        scrollBar.setMaxValue(stacks.size() / slotsWidth + slotsWidth);
        stacks.sort((is1, is2) -> {
            double x = ((IItemBlockColourSaver) is1.getItem()).getScore();
            double y = ((IItemBlockColourSaver) is2.getItem()).getScore();
            return Double.compare(x, y);
        });
        placeSlots();
    }

    public void placeSlots() {
        wColourGuiSlots.forEach(slot -> mainPanel.remove(slot));
        int index = slotsWidth * scrollBar.getValue();
        for (int j = 1; j < slotsHeight; j++) {
            for (int i = 0; i < slotsWidth; i++) {
                if (index >= stacks.size()) break;
                WColourGuiSlot colourGuiSlot = new WColourGuiSlot(stacks.get(index), cg);

                if (wColourGuiSlots.size() <= index) {
                    wColourGuiSlots.add(colourGuiSlot);
                } else {
                    wColourGuiSlots.set(index, colourGuiSlot);
                }
                mainPanel.add(colourGuiSlot, i, j);
                index++;

            }
        }
        mainPanel.validate(this);
    }

    @Override
    public void setColour(ColourVector colour) {
        switch (mode) {
            case RGB:
                rSlider.setValue(colour.r);
                rInput.setText(Integer.toString(colour.r));

                gSlider.setValue(colour.g);
                gInput.setText(Integer.toString(colour.g));

                bSlider.setValue(colour.b);
                bInput.setText(Integer.toString(colour.b));
                break;
            case HSV:
                rSlider.setValue(colour.getHue());
                rInput.setText(Integer.toString(colour.getHue()));

                gSlider.setValue(colour.getSatV());
                gInput.setText(Integer.toString(colour.getSatV()));

                bSlider.setValue(colour.getVal());
                bInput.setText(Integer.toString(colour.getVal()));
                break;
            case HSL:
                rSlider.setValue(colour.getHue());
                rInput.setText(Integer.toString(colour.getHue()));

                gSlider.setValue(colour.getSatL());
                gInput.setText(Integer.toString(colour.getSatL()));

                bSlider.setValue(colour.getLight());
                bInput.setText(Integer.toString(colour.getLight()));
                break;
        }
        inputColour = colour;
        hexInput.setText(inputColour.getHex());
        updateArmour();
        colourSort();
        scrollBar.setValue(0);
        placeSlots();
    }

    public void OpenBlockInfoGui(net.minecraft.client.Minecraft client, MCRGBClient mcrgb_forgeClient, ItemStack stack) {
        client.setScreen(new ColourScreen(new BlockInfoGui(client, mcrgb_forgeClient, stack, inputColour)));
    }

    public void ToggleColourWheel(boolean isToggled) {
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
            mainPanel.add(colourWheel, 11, 2, 6, 6);
            mainPanel.add(wheelValueSlider, 17, 2, 1, 6);
            wheelValueSlider.setValue(wheelValueSlider.getMaxValue());
            colourWheel.setLocation(198, 47);
            wheelValueSlider.setLocation(314, 47);
            wheelValueSlider.setSize(18, 128);
            rLabel.setLocation(211, 165);
            gLabel.setLocation(247, 165);
            bLabel.setLocation(283, 165);
        } else {
            mainPanel.add(sliderArea, 11, 2, 6, 7);
            mainPanel.add(armourSlots, 17, 3);
            mainPanel.remove(colourWheel);
            mainPanel.remove(wheelValueSlider);
            rLabel.setLocation(211, 50);
            gLabel.setLocation(247, 50);
            bLabel.setLocation(283, 50);

        }
        root.validate(this);
    }

    public enum ColourMode {
        RGB, HSV, HSL
    }


}
