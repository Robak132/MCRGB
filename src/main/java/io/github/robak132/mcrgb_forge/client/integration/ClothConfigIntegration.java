package io.github.robak132.mcrgb_forge.client.integration;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;
import static io.github.robak132.mcrgb_forge.config.MCRGBConfig.ALWAYS_SHOW_TOOLTIPS;
import static io.github.robak132.mcrgb_forge.config.MCRGBConfig.BYPASS_OP;
import static io.github.robak132.mcrgb_forge.config.MCRGBConfig.GENERAL_SPEC;
import static io.github.robak132.mcrgb_forge.config.MCRGBConfig.GIVE_COMMAND;
import static io.github.robak132.mcrgb_forge.config.MCRGBConfig.SLIDER_CONSTANT_UPDATE;

import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Slf4j(topic = MOD_ID)
public class ClothConfigIntegration {

    private static final Function<Boolean, Component> alwaysShowToolTipsTextSupplier = value -> value
            ? Component.translatable("options.mcrgb_forge.all_contexts")
            : Component.translatable("options.mcrgb_forge.picker_only");

    private static final Function<Boolean, Component> sliderConstantUpdateTextSupplier = value -> value
            ? Component.translatable("options.mcrgb_forge.while_scrolling")
            : Component.translatable("options.mcrgb_forge.after_scrolling");

    public static Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent)
                .setTitle(Component.translatable("title.mcrgb_forge.config")).setDoesConfirmSave(true);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory configs = builder.getOrCreateCategory(
                Component.translatable("options.mcrgb_forge.category.configs"));

        // Always Show Tooltips toggle
        BooleanToggleBuilder alwaysShowToolTip = entryBuilder.startBooleanToggle(
                Component.translatable("option.mcrgb_forge.always_show_in_tooltips"),
                ALWAYS_SHOW_TOOLTIPS.get());
        alwaysShowToolTip.setDefaultValue(ALWAYS_SHOW_TOOLTIPS.getDefault())
                .setYesNoTextSupplier(alwaysShowToolTipsTextSupplier)
                .setSaveConsumer(ALWAYS_SHOW_TOOLTIPS::set)
                .setTooltip(Component.translatable("tooltip.mcrgb_forge.always_show_in_tooltips")).build();
        configs.addEntry(alwaysShowToolTip.build());

        // Slider Constant Update toggle
        BooleanToggleBuilder sliderConstantUpdate = entryBuilder.startBooleanToggle(
                Component.translatable("option.mcrgb_forge.slider_constant_update"),
                SLIDER_CONSTANT_UPDATE.get());
        sliderConstantUpdate.setDefaultValue(SLIDER_CONSTANT_UPDATE.getDefault())
                .setYesNoTextSupplier(sliderConstantUpdateTextSupplier)
                .setSaveConsumer(SLIDER_CONSTANT_UPDATE::set)
                .setTooltip(Component.translatable("tooltip.mcrgb_forge.slider_constant_update")).build();
        configs.addEntry(sliderConstantUpdate.build());

        // Give Command string field
        StringFieldBuilder commandString = entryBuilder.startStrField(
                Component.translatable("option.mcrgb_forge.give_command"),
                GIVE_COMMAND.get());
        commandString.setDefaultValue(GIVE_COMMAND.getDefault())
                .setSaveConsumer(GIVE_COMMAND::set)
                .setTooltip(Component.translatable("tooltip.mcrgb_forge.give_command")).build();
        configs.addEntry(commandString.build());

        // Bypass OP toggle
        BooleanToggleBuilder bypassOP = entryBuilder.startBooleanToggle(
                Component.translatable("option.mcrgb_forge.bypass_op"), BYPASS_OP.get());
        bypassOP.setDefaultValue(BYPASS_OP.getDefault())
                .setSaveConsumer(BYPASS_OP::set)
                .setTooltip(Component.translatable("tooltip.mcrgb_forge.bypass_op")).build();
        configs.addEntry(bypassOP.build());

        builder.setSavingRunnable(GENERAL_SPEC::save);

        return builder.build();
    }

    private static boolean isClothConfigLoaded() {
        if (ModList.get().isLoaded("cloth_config")) {
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                return true;
            } catch (ClassNotFoundException e) {
                log.error("Cloth Config is installed but ConfigBuilder class not found", e);
            }
        }
        return false;
    }

    public static void init(FMLClientSetupEvent event) {
        if (isClothConfigLoaded()) {
            log.info("Cloth Config detected, registering config screen.");
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (client, parent) -> ClothConfigIntegration.getConfigScreen(parent)
                    )
            );
        } else {
            log.warn("Cloth Config not found, config screen will be unavailable.");
        }
    }
}
