package com.bacco;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class ClothConfigIntegration {
    private static final Function<Boolean, Component> alwaysShowToolTipsTextSupplier = bool -> {
        if (Boolean.TRUE.equals(bool)) return Component.translatable("options.mcrgb_forge.all_contexts");
        else return Component.translatable("options.mcrgb_forge.picker_only");
    };
    private static final Function<Boolean, Component> sliderConstantUpdateTextSupplier = bool -> {
        if (Boolean.TRUE.equals(bool)) return Component.translatable("options.mcrgb_forge.while_scrolling");
        else return Component.translatable("options.mcrgb_forge.after_scrolling");
    };

    public static Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.translatable("title.mcrgb_forge.config")).setDoesConfirmSave(true);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory configs = builder.getOrCreateCategory(Component.translatable("options.mcrgb_forge.category.configs"));

        configs.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.mcrgb_forge.always_show_in_tooltips"),
                MCRGBConfig.instance.alwaysShowToolTips).setDefaultValue(false).setYesNoTextSupplier(alwaysShowToolTipsTextSupplier).setSaveConsumer(newValue -> MCRGBConfig.instance.alwaysShowToolTips = newValue).setTooltip(Component.translatable("tooltip.mcrgb_forge.always_show_in_tooltips")).build());

        configs.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.mcrgb_forge.slider_constant_update"), MCRGBConfig.instance.sliderConstantUpdate).setDefaultValue(true).setYesNoTextSupplier(sliderConstantUpdateTextSupplier).setSaveConsumer(newValue -> MCRGBConfig.instance.sliderConstantUpdate = newValue).setTooltip(Component.translatable("tooltip.mcrgb_forge.slider_constant_update")).build());

        configs.addEntry(entryBuilder.startStrField(Component.translatable("option.mcrgb_forge.give_command"), MCRGBConfig.instance.command).setDefaultValue("give %p %i%c %q").setSaveConsumer(newValue -> MCRGBConfig.instance.command = newValue).setTooltip(Component.translatable("tooltip.mcrgb_forge.give_command")).build());

        configs.addEntry(entryBuilder.startBooleanToggle(Component.translatable("Bypass Operator Check"), MCRGBConfig.instance.bypassOP).setDefaultValue(false).setSaveConsumer(newValue -> MCRGBConfig.instance.bypassOP = newValue).setTooltip(Component.translatable("tooltip.mcrgb_forge.bypass_op")).build());


        builder.setSavingRunnable(MCRGBConfig::save);

        return builder.build();
    }
}

