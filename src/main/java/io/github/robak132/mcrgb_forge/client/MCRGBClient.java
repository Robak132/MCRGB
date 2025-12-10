package io.github.robak132.mcrgb_forge.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.robak132.mcrgb_forge.BlockColourStorage;
import io.github.robak132.mcrgb_forge.ColourGroup;
import io.github.robak132.mcrgb_forge.ColourVector;
import io.github.robak132.mcrgb_forge.IItemBlockColourSaver;
import io.github.robak132.mcrgb_forge.MCRGB;
import io.github.robak132.mcrgb_forge.Palette;
import io.github.robak132.mcrgb_forge.SpriteDetails;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
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
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = MCRGB.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@Slf4j(topic = MCRGB.MOD_ID)
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
    public static Set<ColourGroup> groupColours(List<ColourVector> rgblist) {
        Set<ColourGroup> groups = new HashSet<>();

        //Loop through every pixel
        for (int i = 0; i < rgblist.size(); i++) {
            ColourVector iPix = new ColourVector(rgblist.get(i).r, rgblist.get(i).g, rgblist.get(i).b);

            //check if already in a group
            boolean iInGroup = false;
            for (ColourGroup group : groups) {
                if (group.getPixels().contains(iPix)) {
                    iInGroup = true;
                    break;
                }
            }

            //if I'm not in a group, create a new one and add i to it...
            if (!iInGroup) {
                ColourGroup newGroup = new ColourGroup();
                newGroup.addPixel(iPix);

                //loop through all the pixels after i, and compare them to i
                for (int j = i + 1; j < rgblist.size(); j++) {
                    //if the distance is less than 100, add j to the group (if it is not already in a group)
                    ColourVector jPix = new ColourVector(rgblist.get(j).r, rgblist.get(j).g, rgblist.get(j).b);
                    if (jPix.distance(iPix) < 100) {
                        boolean jInGroup = false;
                        for (ColourGroup group : groups) {
                            if (group.getPixels().contains(jPix)) {
                                jInGroup = true;
                                break;
                            }
                        }

                        if (!jInGroup) {
                            newGroup.addPixel(jPix);
                        }
                    }

                }
                //finally, add the new group to the list of groups
                groups.add(newGroup);
            }
        }
        //calculate the average rgb value of each group, convert to hex and calculate weight
        for (ColourGroup group : groups) {
            ColourVector sum = new ColourVector(0, 0, 0);
            int counter = 0;
            for (ColourVector colour : group.getPixels()) {
                sum.add(colour);
                counter++;
            }
            if (counter == 0) {
                return Collections.emptySet();
            }
            ColourVector avg = sum.div(counter);
            group.setMeanColour(avg);
            group.setMeanHex(avg.getHex());
            group.setWeight((int) ((float) counter / (float) rgblist.size() * 100));
        }

        return groups;
    }

    public static void refreshColours() {
        Minecraft minecraft = Minecraft.getInstance();
        //get top sprite of stone block default state
        IForgeBakedModel bakedModel = minecraft.getModelManager().getBlockModelShaper().getBlockModel(Blocks.STONE.defaultBlockState());
        TextureAtlasSprite defSprite = bakedModel.getQuads(Blocks.STONE.defaultBlockState(), Direction.UP, RandomSource.create(), ModelData.EMPTY, null).get(0)
                .getSprite();
        //get id of the atlas containing above
        //use atlas id to get OpenGL ID. Atlas contains ALL blocks
        int glID = minecraft.getTextureManager().getTexture(defSprite.atlasLocation()).getId();
        //get width and height from OpenGL by binding texture
        RenderSystem.bindTexture(glID);
        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        int size = width * height;
        //Make byte buffer and load full atlas into buffer.
        ByteBuffer buffer = BufferUtils.createByteBuffer(size * 4);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        //convert buffer to an array of bytes
        byte[] pixels = new byte[size * 4];
        buffer.get(pixels);
        List<BlockColourStorage> blockColourList = new ArrayList<>();
        //loop through every block in the game
        ForgeRegistries.BLOCKS.forEach(block -> {
            if (block.asItem().getDescriptionId().equals(Items.AIR.getDescriptionId())) {
                return;
            }
            ((IItemBlockColourSaver) block.asItem()).mcrgb_forge$clearSpriteDetails();
            BlockColourStorage storage = new BlockColourStorage();
            instance.totalBlocks += 1;
            Set<TextureAtlasSprite> sprites = getSprites(block);
            if (sprites.isEmpty()) {
                return;
            }
            sprites.forEach(sprite -> {

                //get coords of sprite in atlas
                int spriteX = sprite.getX();
                int spriteY = sprite.getY();
                int spriteW = sprite.contents().width();
                int spriteH = sprite.contents().height();
                //convert coords to byte position
                int firstPixel = (spriteY * width + spriteX) * 4;
                List<ColourVector> rgbList = new ArrayList<>();
                int biomeColour = 0xFFFFFF;
                try {
                    biomeColour = minecraft.getBlockColors().getColor(block.defaultBlockState(), null, null, 0);
                } catch (Exception e) {
                    log.warn("Could not find biome colour for block: {}. Please report this logfile to https://github.com/bacco-bacco/MCRGB/issues",
                            block.getName());
                }
                //for each horizontal row in the sprite
                for (int row = 0; row < spriteH; row++) {
                    int firstInRow = firstPixel + row * width * 4;
                    //loop from first pixel in row to the sprite width.
                    //Note: Looping in increments of 4, because each pixel is 4 bytes. (R,G,B and A)
                    for (int pos = firstInRow; pos < firstInRow + 4 * spriteW; pos += 4) {
                        //retrieve bytes for RGBA values
                        //"& 0xFF" does logical and with 11111111. this extracts the last 8 bits, converting to unsigned int
                        int pixelColour = FastColor.ARGB32.color(pixels[pos + 3], pixels[pos] & 0xFF, pixels[pos + 1] & 0xFF, pixels[pos + 2] & 0xFF);
                        int alpha = FastColor.ARGB32.alpha(pixelColour);
                        if (biomeColour != -1 && (!block.defaultBlockState().is(Blocks.GRASS_BLOCK) || sprite.contents().name().getPath().equals("block/grass_block_top"))) {
                            pixelColour = FastColor.ARGB32.multiply(biomeColour, pixelColour);
                        }
                        //if the pixel is not fully transparent, add to the list
                        if (alpha > 0) {
                            rgbList.add(new ColourVector(FastColor.ARGB32.red(pixelColour), FastColor.ARGB32.green(pixelColour),
                                    FastColor.ARGB32.blue(pixelColour)));
                        }
                    }
                }
                //Calculate the dominant colours
                Set<ColourGroup> colourGroups = groupColours(rgbList);

                //Add sprite name and each dominant color to the IItemBlockColourSaver
                SpriteDetails spriteDetails = new SpriteDetails();
                String[] namesplit = sprite.contents().name().toString().split("/");
                spriteDetails.setName(namesplit[namesplit.length - 1]);
                colourGroups.forEach(cg -> {
                    spriteDetails.addColour(cg.getMeanColour());
                    spriteDetails.addWeight(cg.getWeight());
                });
                storage.setBlock(block.asItem().getDescriptionId());
                storage.addSpriteDetails(spriteDetails);
            });
            IItemBlockColourSaver iItemBlockColourSaver = (IItemBlockColourSaver) block.asItem();
            storage.getSpriteDetails().forEach(iItemBlockColourSaver::mcrgb_forge$addSpriteDetails);
            blockColourList.add(storage);
        });

        //Write arraylist to json
        writeJson(gson.toJson(blockColourList), "./mcrgb_forge_colours/", "file.json");
        minecraft.player.displayClientMessage(Component.translatable("message.mcrgb_forge.reloaded"), false);
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
        writeJson(gson.toJson(instance.palettes), "./mcrgb_forge_colours/", "palettes.json");
    }

    public static void loadPalettes() {
        instance.palettes = readJson("./mcrgb_forge_colours/palettes.json", new TypeToken<List<Palette>>() {
        }, new ArrayList<>());
    }

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (isScanned()) {
            return;
        }
        try {
            List<BlockColourStorage> loadedBlockColourArray = readJson("./mcrgb_forge_colours/file.json", new TypeToken<List<BlockColourStorage>>() {
            }, new ArrayList<>());
            ForgeRegistries.BLOCKS.forEach(block -> {
                for (BlockColourStorage storage : loadedBlockColourArray) {
                    if (storage.getBlock().equals(block.asItem().getDescriptionId())) {
                        IItemBlockColourSaver blockColourSaver = (IItemBlockColourSaver) block.asItem();
                        storage.getSpriteDetails().forEach(blockColourSaver::mcrgb_forge$addSpriteDetails);
                        break;
                    }
                }
            });
            setScanned(true);
        } catch (Exception e) {
            refreshColours();
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!MCRGBConfig.ALWAYS_SHOW_TOOLTIPS.get()) {
            return;
        }
        IItemBlockColourSaver item = (IItemBlockColourSaver) event.getItemStack().getItem();
        for (int i = 0; i < item.mcrgb_forge$getLength(); i++) {
            List<String> strings = item.mcrgb_forge$getSpriteDetails(i).getStrings();
            List<Integer> colours = item.mcrgb_forge$getSpriteDetails(i).getTextColours();
            if (!strings.isEmpty()) {
                if (Screen.hasShiftDown()) {
                    for (int j = 0; j < strings.size(); j++) {
                        MutableComponent text = Component.literal(strings.get(j)).withStyle(ChatFormatting.GRAY);
                        MutableComponent text2 = (MutableComponent) Component.literal("⬛").toFlatList(Style.EMPTY.withColor(colours.get(j))).get(0);
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