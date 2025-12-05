package com.bacco.event;

import com.bacco.ColourVector;
import com.bacco.gui.ColourGui;
import com.bacco.gui.ColourScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_MCRGB = "key.category.mcrgb_forge.mcrgb_forge";
    public static final String KEY_COLOUR_INV_OPEN = "key.mcrgb_forge.colour_inv_open";
    public static KeyMapping colourInvKey;

    public static void registerStatic() {
        colourInvKey = new KeyMapping(
            KEY_COLOUR_INV_OPEN,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            KEY_CATEGORY_MCRGB
        );
    }

    @Mod.EventBusSubscriber(modid = "mcrgb_forge", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            if (colourInvKey != null) {
                event.register(colourInvKey);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "mcrgb_forge", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (colourInvKey != null && colourInvKey.consumeClick()){
                if (mc.screen == null) {
                    mc.setScreen(new ColourScreen(new ColourGui(mc, new ColourVector(0xFFFFFFFF))));
                } else {
                    mc.setScreen(null);
                }
            }
        }
    }
}
