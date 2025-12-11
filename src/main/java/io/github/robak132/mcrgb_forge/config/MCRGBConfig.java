package io.github.robak132.mcrgb_forge.config;

import io.github.robak132.mcrgb_forge.MCRGBMod;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minecraftforge.common.ForgeConfigSpec;

@Data
@Slf4j(topic = MCRGBMod.MOD_ID)
public final class MCRGBConfig {
    public static final ForgeConfigSpec GENERAL_SPEC;
    public static final ForgeConfigSpec.BooleanValue BYPASS_OP;
    public static final ForgeConfigSpec.BooleanValue ALWAYS_SHOW_TOOLTIPS;
    public static final ForgeConfigSpec.BooleanValue SLIDER_CONSTANT_UPDATE;
    public static final ForgeConfigSpec.ConfigValue<String> GIVE_COMMAND;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        ALWAYS_SHOW_TOOLTIPS = configBuilder.define("alwaysShowToolTips", false);
        SLIDER_CONSTANT_UPDATE = configBuilder.define("sliderConstantUpdate", true);
        BYPASS_OP = configBuilder.define("bypassOP", false);
        GIVE_COMMAND = configBuilder.define("command", "give %p %i%c %q");
        GENERAL_SPEC = configBuilder.build();
    }
}
