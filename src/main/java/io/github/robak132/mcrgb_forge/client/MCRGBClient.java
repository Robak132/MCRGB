package io.github.robak132.mcrgb_forge.client;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;
import static io.github.robak132.mcrgb_forge.client.utils.ChatUtils.displayClientMessage;

import io.github.robak132.libgui_forge.client.CottonClientScreen;
import io.github.robak132.mcrgb_forge.client.analysis.ColorScanner;
import io.github.robak132.mcrgb_forge.client.analysis.ColorScanner.ScanResult;
import io.github.robak132.mcrgb_forge.client.analysis.Palette;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import io.github.robak132.mcrgb_forge.client.gui.ColorsGuiDescription;
import io.github.robak132.mcrgb_forge.client.integration.ClothConfigIntegration;
import io.github.robak132.mcrgb_forge.client.serialization.CacheSerializer;
import io.github.robak132.mcrgb_forge.client.serialization.PaletteSerializer;
import io.github.robak132.mcrgb_forge.colors.RGB;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
@Slf4j(topic = MOD_ID)
public class MCRGBClient {

    public static Map<Block, List<SpriteDetails>> lastScan;

    private static final ColorScanner scanner = new ColorScanner();
    private static final PaletteSerializer paletteSerializer = new PaletteSerializer();
    private static final CacheSerializer cacheSerializer = new CacheSerializer();
    private static Future<ScanResult> activeScan = null;

    @Getter
    private static List<Palette> palettes;

    private MCRGBClient() {}

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ClothConfigIntegration::init);
    }

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (palettes == null) {
            loadPalettes();
        }

        // Load cached color scan
        Map<Block, List<SpriteDetails>> cached = cacheSerializer.load();
        if (cached != null) {
            lastScan = cached;
            displayClientMessage("MCRGB: Startup cache loaded, %s blocks analyzed.", cached.size());
        } else {
            triggerScan();
            displayClientMessage("MCRGB: No startup color cache found. Starting scan...");
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (activeScan != null) {
            activeScan.cancel(true);
        }
        lastScan = null;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
            return;
        }

        if (MCRGBKeybindings.OPEN_GUI.consumeClick()) {
            openColorsGui();
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!MCRGBConfig.ALWAYS_SHOW_TOOLTIPS.get()) {
            return;
        }

        Map<Block, List<SpriteDetails>> data = lastScan;
        if (data == null) {
            return;
        }

        Block block = Block.byItem(event.getItemStack().getItem());

        List<SpriteDetails> sprites = data.get(block);
        if (sprites == null || sprites.isEmpty()) {
            return;
        }

        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("tooltip.mcrgb_forge.shift_prompt").withStyle(ChatFormatting.GRAY));
            return;
        }

        // Show clustered colors
        for (SpriteDetails sd : sprites) {
            List<String> labels = sd.getStrings();
            List<Integer> colors = sd.getTextColors();

            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                int color = colors.get(i);

                MutableComponent colorBlock = Component.literal("⬛").withStyle(Style.EMPTY.withColor(color));

                MutableComponent text = Component.literal(label).withStyle(ChatFormatting.GRAY);

                event.getToolTip().add(colorBlock.append(text));
            }
        }
    }

    public static void addPalette(Palette palette) {
        palettes.add(palette);
    }

    public static void removePalette(Palette palette) {
        palettes.remove(palette);
    }

    public static void savePalettes() {
        paletteSerializer.save(palettes);
    }

    public static void loadPalettes() {
        palettes = paletteSerializer.load();
    }

    public static void triggerScan() {
        if (activeScan != null && !activeScan.isDone()) return;
        activeScan = scanner.scanAsync(MCRGBClient::onSuccess, MCRGBClient::onError);
    }

    private static void openColorsGui() {
        Minecraft mc = Minecraft.getInstance();
        if (lastScan == null) {
            displayClientMessage("MCRGB: No cached color data available. Scan in progress...");
            return;
        }
        if (mc.player == null || mc.level == null) return;
        mc.setScreen(new CottonClientScreen(new ColorsGuiDescription(new RGB(255, 255, 255), lastScan)));
    }

    private static void onSuccess(ScanResult result) {
        if (Thread.currentThread().isInterrupted()) return;
        Minecraft.getInstance().execute(() -> {
            lastScan = result.blockSprites();
            cacheSerializer.save(lastScan);
            displayClientMessage("MCRGB: Color scan completed, %d blocks analyzed.", lastScan.size());
        });
    }

    private static void onError(Throwable error) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            log.error("Color scan failed", error);
            mc.gui.setOverlayMessage(Component.literal("Color scan failed."), false);
        });
    }
}
