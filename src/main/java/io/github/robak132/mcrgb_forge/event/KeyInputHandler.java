package io.github.robak132.mcrgb_forge.event;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.mcrgb_forge.client.gui.ColorGui;
import io.github.robak132.mcrgb_forge.client.gui.ColorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_MCRGB = "key.category.mcrgb_forge.mcrgb_forge";
    public static final String KEY_COLOR_INV_OPEN = "key.mcrgb_forge.color_inv_open";
    public static KeyMapping colorInvKey;

    public static void registerStatic() {
        colorInvKey = new KeyMapping(
            KEY_COLOR_INV_OPEN,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            KEY_CATEGORY_MCRGB
        );
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            if (colorInvKey != null) {
                event.register(colorInvKey);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (colorInvKey != null && colorInvKey.consumeClick()){
                if (mc.screen == null) {
                    mc.setScreen(new ColorScreen(new ColorGui(new ColorVector(0xFFFFFFFF))));
                } else {
                    mc.setScreen(null);
                }
            }
        }
    }
}
