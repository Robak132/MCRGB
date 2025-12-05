package com.bacco;

import com.bacco.event.KeyInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MCRGB.MCRGB_MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MCRGBClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        KeyInputHandler.registerStatic();
        MCRGBConfig.load();
        MCRGBClient.client = Minecraft.getInstance();
        MCRGBClient.loadPalettes();
    }
}