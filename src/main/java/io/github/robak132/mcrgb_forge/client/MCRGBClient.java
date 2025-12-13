package io.github.robak132.mcrgb_forge.client;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;
import static io.github.robak132.mcrgb_forge.client.utils.ChatUtils.displayClientMessage;

import com.mojang.blaze3d.platform.InputConstants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MOD_ID)
@Slf4j(topic = MOD_ID)
public class MCRGBClient {

    public static final String KEY_CATEGORY_MCRGB = "key.category.mcrgb_forge.mcrgb_forge";
    public static final String KEY_COLOR_INV_OPEN = "key.mcrgb_forge.color_inv_open";
    public static Map<Block, List<SpriteDetails>> lastScan;

    private static final Minecraft MC = Minecraft.getInstance();
    private static final KeyMapping OPEN_GUI = new KeyMapping(KEY_COLOR_INV_OPEN, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY_MCRGB);
    private static final ColorScanner SCANNER = new ColorScanner();
    private static final PaletteSerializer paletteSerializer = new PaletteSerializer();
    private static final CacheSerializer cacheSerializer = new CacheSerializer();
    private static Future<?> activeScan = null;

    @Getter
    private static List<Palette> palettes = new ArrayList<>();

    private MCRGBClient() { }

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

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(OPEN_GUI);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || MC.player == null) {
            return;
        }

        if (OPEN_GUI.consumeClick()) {
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

    public static void triggerScan() {
        if (activeScan != null && !activeScan.isDone()) return;
        activeScan = SCANNER.scanAsync(ForgeRegistries.BLOCKS.getEntries().stream().map(Entry::getValue).toList(),
                result -> MC.execute(() -> onScanComplete(result)),
                error -> MC.execute(() -> MC.gui.setOverlayMessage(Component.literal("Color scan failed."), false)));
    }

    private static void onScanComplete(ScanResult result) {
        lastScan = result.blockSprites();
        cacheSerializer.save(lastScan);
        displayClientMessage("MCRGB: Color scan completed, %d blocks analyzed.", lastScan.size());
    }

    private static void openColorsGui() {
        if (lastScan == null) {
            displayClientMessage("MCRGB: No cached color data available. Scan in progress...");
            return;
        }
        MC.setScreen(new CottonClientScreen(new ColorsGuiDescription(new RGB(255, 255, 255), lastScan)));
    }
}
