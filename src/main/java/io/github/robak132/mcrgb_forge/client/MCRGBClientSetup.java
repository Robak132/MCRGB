package io.github.robak132.mcrgb_forge.client;

import io.github.robak132.mcrgb_forge.MCRGBMod;
import io.github.robak132.mcrgb_forge.event.KeyInputHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MCRGBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MCRGBClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        KeyInputHandler.registerStatic();
        MCRGBClient.loadPalettes();
    }
}