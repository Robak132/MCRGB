package io.github.robak132.mcrgb_forge;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.mcrgb_forge.client.MCRGBClient;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;
import lombok.extern.slf4j.Slf4j;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MOD_ID)
@Slf4j(topic = MCRGBMod.MOD_ID)
public class MCRGBMod {

    public static final String MOD_ID = "mcrgb_forge";

    public MCRGBMod() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MCRGBConfig.GENERAL_SPEC, MOD_ID + ".toml");
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> MCRGBClient::init);
    }

}
