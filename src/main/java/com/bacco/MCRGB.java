package com.bacco;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static com.bacco.MCRGB.MCRGB_MOD_ID;

@Mod(MCRGB_MOD_ID)
public class MCRGB {
    public static final String MCRGB_MOD_ID = "mcrgb_forge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MCRGB_MOD_ID);

    public MCRGB() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static boolean isClothConfigLoaded() {
        if (ModList.get().isLoaded("cloth-config-forge")) {
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Path getConfigFolder() {
        return FMLLoader.getGamePath().resolve("config");
    }

    public void clientSetup(FMLClientSetupEvent event) {
        if (isClothConfigLoaded()) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
              () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> ClothConfigIntegration.getConfigScreen(parent)));
        }
    }

}