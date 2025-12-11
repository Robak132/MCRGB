package io.github.robak132.mcrgb_forge.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.robak132.mcrgb_forge.MCRGBMod;
import io.github.robak132.mcrgb_forge.client.analysis.BlockColorStorage;
import io.github.robak132.mcrgb_forge.client.analysis.ColorClustering;
import io.github.robak132.mcrgb_forge.client.analysis.ColorVector;
import io.github.robak132.mcrgb_forge.client.analysis.IItemBlockColorSaver;
import io.github.robak132.mcrgb_forge.client.analysis.Palette;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteColor;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import io.github.robak132.mcrgb_forge.client.integration.ClothConfigIntegration;
import io.github.robak132.mcrgb_forge.config.MCRGBConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = MCRGBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@Slf4j(topic = MCRGBMod.MOD_ID)
public class MCRGBClient {

    @Getter
    private static final MCRGBClient instance = new MCRGBClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private List<Palette> palettes = new ArrayList<>();
    private int totalBlocks = 0;
    private int fails = 0;
    private int successes = 0;
    private boolean scanned = false;

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

    public static int getTotalBlocks() {
        return instance.totalBlocks;
    }

    public static void setTotalBlocks(int totalBlocks) {
        instance.totalBlocks = totalBlocks;
    }

    public static int getFails() {
        return instance.fails;
    }

    public static void setFails(int fails) {
        instance.fails = fails;
    }

    public static int getSuccesses() {
        return instance.successes;
    }

    public static void setSuccesses(int successes) {
        instance.successes = successes;
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

    //Calculate the dominant colors in a list of colors
    public static void refreshColors() {
        Minecraft mc = Minecraft.getInstance();

        // Reset counters
        instance.totalBlocks = 0;
        instance.successes = 0;
        instance.fails = 0;

        List<BlockColorStorage> result = new ArrayList<>();

        // Loop through all blocks
        ForgeRegistries.BLOCKS.forEach(block -> {
            if (block == Blocks.AIR) {
                return;
            }

            instance.totalBlocks++;

            IItemBlockColorSaver saver = (IItemBlockColorSaver) block.asItem();
            saver.mcrgb_forge$clearSpriteDetails();

            BlockColorStorage storage = new BlockColorStorage();
            storage.setBlockId(block.asItem().getDescriptionId());

            Set<TextureAtlasSprite> sprites = getSprites(block);
            if (sprites.isEmpty()) {
                return;
            }

            for (TextureAtlasSprite sprite : sprites) {
                SpriteContents contents = sprite.contents();
                NativeImage image = contents.getOriginalImage();
                if (image == null) {
                    instance.fails++;
                    continue;
                }

                int w = contents.width();
                int h = contents.height();

                List<ColorVector> pixelList = new ArrayList<>(w * h);

                // biome tint
                int biomeColor = 0xFFFFFF;
                try {
                    biomeColor = mc.getBlockColors().getColor(block.defaultBlockState(), null, null, 0);
                } catch (Exception ignored) {
                }

                boolean applyBiomeTint =
                        biomeColor != -1 && (!block.defaultBlockState().is(Blocks.GRASS_BLOCK) || contents.name().getPath().equals("block/grass_block_top"));

                // Read pixels
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int argb = image.getPixelRGBA(x, y);

                        int a = (argb >>> 24) & 0xFF;
                        if (a == 0) {
                            continue;
                        }

                        if (applyBiomeTint) {
                            argb = FastColor.ARGB32.multiply(biomeColor, argb);
                        }

                        int r = FastColor.ARGB32.red(argb);
                        int g = FastColor.ARGB32.green(argb);
                        int b = FastColor.ARGB32.blue(argb);

                        pixelList.add(new ColorVector(r, g, b));
                    }
                }

                if (pixelList.isEmpty()) {
                    continue;
                }

                List<SpriteColor> dominant = ColorClustering.kMeansOkLab(pixelList, 3, 8, 4096);

                SpriteDetails details = new SpriteDetails();
                details.setName(sprite.contents().name().getPath());

                for (SpriteColor sc : dominant) {
                    details.add(sc);
                }

                storage.addSpriteDetails(details);
                saver.mcrgb_forge$addSpriteDetails(details);

                instance.successes++;
            }

            result.add(storage);
        });

        // Save file
        writeJson(gson.toJson(result), "./mcrgb_forge_colors/", "file.json");

        Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.mcrgb_forge.reloaded"), false);
    }

    public static @NotNull Set<TextureAtlasSprite> getSprites(Block block) {
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        block.getStateDefinition().getPossibleStates().forEach(state -> {
            Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, null};
            for (Direction direction : directions) {
                try {
                    IForgeBakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
                    sprites.add(model.getQuads(state, direction, RandomSource.create(), ModelData.EMPTY, null).get(0).getSprite());
                    instance.successes += 1;
                } catch (Exception e) {
                    instance.fails += 1;
                }
            }
        });
        return sprites;
    }

    public static void savePalettes() {
        writeJson(gson.toJson(instance.palettes), "./mcrgb_forge_colors/", "palettes.json");
    }

    public static void loadPalettes() {
        instance.palettes = readJson("./mcrgb_forge_colors/palettes.json", new TypeToken<List<Palette>>() {
        }, new ArrayList<>());
    }

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (isScanned()) {
            return;
        }
        try {
            List<BlockColorStorage> loadedBlockColorArray = readJson("./mcrgb_forge_colors/file.json", new TypeToken<List<BlockColorStorage>>() {
            }, new ArrayList<>());
            ForgeRegistries.BLOCKS.forEach(block -> {
                for (BlockColorStorage storage : loadedBlockColorArray) {
                    if (storage.getBlockId().equals(block.asItem().getDescriptionId())) {
                        IItemBlockColorSaver blockColorSaver = (IItemBlockColorSaver) block.asItem();
                        storage.getSpriteDetails().forEach(blockColorSaver::mcrgb_forge$addSpriteDetails);
                        break;
                    }
                }
            });
            setScanned(true);
        } catch (Exception e) {
            refreshColors();
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!MCRGBConfig.ALWAYS_SHOW_TOOLTIPS.get()) {
            return;
        }
        IItemBlockColorSaver item = (IItemBlockColorSaver) event.getItemStack().getItem();
        for (int i = 0; i < item.mcrgb_forge$getLength(); i++) {
            List<String> strings = item.mcrgb_forge$getSpriteDetails(i).getStrings();
            List<Integer> colors = item.mcrgb_forge$getSpriteDetails(i).getTextColors();
            if (!strings.isEmpty()) {
                if (Screen.hasShiftDown()) {
                    for (int j = 0; j < strings.size(); j++) {
                        MutableComponent text = Component.literal(strings.get(j)).withStyle(ChatFormatting.GRAY);
                        MutableComponent text2 = (MutableComponent) Component.literal("⬛").toFlatList(Style.EMPTY.withColor(colors.get(j))).get(0);
                        if (j > 0) {
                            text2.append(text);
                        } else {
                            text2 = text.withStyle(ChatFormatting.DARK_GRAY);
                        }

                        event.getToolTip().add(text2);
                    }
                } else {
                    var text = Component.translatable("tooltip.mcrgb_forge.shift_prompt");
                    var message = text.withStyle(ChatFormatting.GRAY);
                    event.getToolTip().add(message);
                    break;
                }
            }
        }
    }

}