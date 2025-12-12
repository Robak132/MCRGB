package io.github.robak132.mcrgb_forge.client;

import static io.github.robak132.mcrgb_forge.MCRGBMod.MOD_ID;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.robak132.libgui_forge.client.CottonClientScreen;
import io.github.robak132.mcrgb_forge.client.analysis.ColorScanner;
import io.github.robak132.mcrgb_forge.client.analysis.ColorScanner.ScanResult;
import io.github.robak132.mcrgb_forge.client.analysis.Palette;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import io.github.robak132.mcrgb_forge.client.gui.ColorsGuiDescription;
import io.github.robak132.mcrgb_forge.client.integration.ClothConfigIntegration;
import io.github.robak132.mcrgb_forge.colors.Color.ColorModel;
import io.github.robak132.mcrgb_forge.colors.RGB;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MOD_ID)
@Slf4j(topic = MOD_ID)
public class MCRGBClient {

    public static final String KEY_CATEGORY_MCRGB = "key.category.mcrgb_forge.mcrgb_forge";
    public static final String KEY_COLOR_INV_OPEN = "key.mcrgb_forge.color_inv_open";
    private static final MCRGBClient instance = new MCRGBClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final KeyMapping OPEN_GUI = new KeyMapping(KEY_COLOR_INV_OPEN, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY_MCRGB);
    private static final ColorScanner SCANNER = new ColorScanner();
    private static final Path CACHE_FILE = Path.of("config", "mcrgb_cache.json");
    private static Future<?> activeScan = null;
    private static boolean scanRequested = false;
    private static Map<Block, List<SpriteDetails>> lastScan = null;
    private List<Palette> palettes = new ArrayList<>();
    private boolean scanned = false;

    private MCRGBClient() {
    }

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClothConfigIntegration::init);
    }

    public static boolean isScanned() {
        return instance.scanned;
    }

    public static void setScanned(boolean scanned) {
        instance.scanned = scanned;
    }

    public static List<Palette> getPalettes() {
        return instance.palettes;
    }

    public static void setPalettes(List<Palette> palettes) {
        instance.palettes = palettes;
    }

    public static void addPalette(Palette palette) {
        instance.palettes.add(palette);
    }

    public static void removePalette(Palette palette) {
        instance.palettes.remove(palette);
    }

    public static void writeJson(String str, String path, String fileName) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            Files.writeString(Path.of(path + fileName), str, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Could not write json to file: {}", e.getMessage());
        }
    }

    public static <T> T readJson(String path, TypeToken<T> type, T defaultValue) {
        try {
            String str = Files.readString(Path.of(path));
            T data = gson.fromJson(str, type.getType());
            return data != null ? data : defaultValue;
        } catch (IOException e) {
            log.warn("Failed to read {}: {}", path, e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to parse {}: {}", path, e.getMessage());
        }
        return defaultValue;
    }

    public static void savePalettes() {
        writeJson(gson.toJson(instance.palettes), "./mcrgb_forge_colors/", "palettes.json");
    }

    public static void loadPalettes() {
        instance.palettes = readJson("./mcrgb_forge_colors/palettes.json", new TypeToken<List<Palette>>() {
        }, new ArrayList<>());
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
            scanRequested = true;
        }

        if (scanRequested) {
            scanRequested = false;

            // If cache exists, load and open GUI
            if (loadCacheIfExists()) {
                openColorsGui();
            } else {
                // Otherwise compute fresh data
                triggerScan();
            }
        }
    }

    public static boolean loadCacheIfExists() {
        if (!Files.exists(CACHE_FILE)) {
            return false;
        }

        try (Reader reader = Files.newBufferedReader(CACHE_FILE)) {
            Gson gson = new Gson();
            var type = new TypeToken<Map<String, List<SpriteDetails>>>() {
            }.getType();

            Map<String, List<SpriteDetails>> raw = gson.fromJson(reader, type);
            if (raw == null) {
                return false;
            }

            var reg = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BLOCK);
            Map<Block, List<SpriteDetails>> mapped = new HashMap<>();

            for (var e : raw.entrySet()) {
                try {
                    ResourceLocation rl = ResourceLocation.parse(e.getKey());
                    Block b = reg.get(rl);
                    if (b != null) {
                        mapped.put(b, e.getValue());
                    }
                } catch (Exception ex) {
                    // malformed key — skip
                }
            }

            lastScan = mapped;
            return !mapped.isEmpty();
        } catch (Exception e) {
            System.err.println("MCRGB: Failed to load cache: " + e);
            return false;
        }
    }

    private static void saveCache(Map<Block, List<SpriteDetails>> data) {
        try {
            Files.createDirectories(CACHE_FILE.getParent());

            var reg = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BLOCK);

            // Convert Block -> resource string
            Map<String, List<SpriteDetails>> stringMap = new HashMap<>();
            for (var e : data.entrySet()) {
                Block b = e.getKey();
                try {
                    // find the registry key for the block
                    ResourceLocation rl = reg.getKey(b);
                    if (rl == null) {
                        // fallback: skip blocks without a registry key (shouldn't happen)
                        continue;
                    }
                    stringMap.put(rl.toString(), e.getValue());
                } catch (Exception ex) {
                    // fallback: skip this block
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(CACHE_FILE)) {
                gson.toJson(stringMap, writer);
            }
        } catch (IOException e) {
            System.err.println("MCRGB: Failed to save cache: " + e);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!MCRGBConfig.ALWAYS_SHOW_TOOLTIPS.get()) {
            return;
        }

        Map<Block, List<SpriteDetails>> data = MCRGBClient.getLastScan();
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
        if (activeScan != null && !activeScan.isDone()) {
            return;
        }

        List<Block> blocks = MC.level.registryAccess().registryOrThrow(Registries.BLOCK).entrySet().stream().map(Entry::getValue).toList();

        activeScan = SCANNER.scanAsync(blocks, result -> MC.execute(() -> onScanComplete(result)),
                error -> MC.execute(() -> MC.gui.setOverlayMessage(Component.literal("Color scan failed."), false)));
    }

    private static void onScanComplete(ScanResult result) {
        lastScan = result.blockSprites();
        saveCache(lastScan);
    }

    public static Map<Block, List<SpriteDetails>> getLastScan() {
        return lastScan == null ? null : lastScan;
    }

    private static void openColorsGui() {
        if (lastScan == null) {
            return;
        }
        MC.setScreen(new CottonClientScreen(new ColorsGuiDescription(new RGB(255, 255, 255), lastScan)));
    }
}
