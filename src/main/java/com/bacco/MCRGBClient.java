package com.bacco;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.util.*;

@Mod.EventBusSubscriber(modid = MCRGB.MCRGB_MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MCRGBClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("mcrgb_forge");
    public static Minecraft client;
    public static List<Palette> palettes = new ArrayList<>();
    static int totalBlocks = 0;
    static int fails = 0;
    static int successes = 0;
    static volatile boolean scanned = false;

    public static void writeJson(String str, String path, String fileName) {
        File dir = new File(path);
        File file = new File(dir, fileName);
        try (FileWriter fw = new FileWriter(file)) {
            if (!dir.exists()) {
                dir.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            for (int i = 0; i < str.length(); i++) {
                fw.write(str.charAt(i));
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static String readJson(String path) {
        try (FileReader fileReader = new FileReader(path)) {
            int i;
            StringBuilder str = new StringBuilder();
            // Using read method
            while ((i = fileReader.read()) != -1) {
                str.append((char) i);
            }
            return str.toString();
        } catch (Exception e) {
            return "";
        }
    }

    //Calculate the dominant colours in a list of colours
    public static Set<ColourGroup> groupColours(List<ColourVector> rgblist) {
        Set<ColourGroup> groups = new HashSet<>();

        //Loop through every pixel
        for (int i = 0; i < rgblist.size(); i++) {
            ColourVector iPix = new ColourVector(rgblist.get(i).r, rgblist.get(i).g, rgblist.get(i).b);

            //check if already in a group
            boolean iInGroup = false;
            for (ColourGroup group : groups) {
                if (group.pixels.contains(iPix)) {
                    iInGroup = true;
                    break;
                }
            }

            //if i is not in a group, create a new one and add i to it...
            if (!iInGroup) {
                ColourGroup newGroup = new ColourGroup();
                newGroup.pixels.add(iPix);

                //loop through all the pixels after i, and compare them to i
                for (int j = i + 1; j < rgblist.size(); j++) {
                    //if the distance is less than 100, add j to the group (if it is not already in a group)
                    ColourVector jPix = new ColourVector(rgblist.get(j).r, rgblist.get(j).g, rgblist.get(j).b);
                    if (jPix.distance(iPix) < 100) {
                        boolean jInGroup = false;
                        for (ColourGroup group : groups) {
                            if (group.pixels.contains(jPix)) {
                                jInGroup = true;
                                break;
                            }
                        }

                        if (!jInGroup) {
                            newGroup.pixels.add(jPix);
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
            for (ColourVector colour : group.pixels) {
                sum.add(colour);
                counter++;
            }
            if (counter == 0) {
                return Collections.emptySet();
            }
            ColourVector avg = sum.div(counter);
            group.meanColour = new ColourVector(avg.r, avg.g, avg.b);
            group.meanHex = group.meanColour.getHex();
            group.weight = (int) ((float) counter / (float) rgblist.size() * 100);
        }

        return groups;
    }

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (scanned) return;
        try {
            BlockColourStorage[] loadedBlockColourArray = new Gson().fromJson(readJson("./mcrgb_forge_colours/file.json"), BlockColourStorage[].class);
            ForgeRegistries.BLOCKS.forEach(block -> {
                for (BlockColourStorage storage : loadedBlockColourArray) {
                    if (storage.block.equals(block.asItem().getDescriptionId())) {
                        storage.spriteDetails.forEach(details -> ((IItemBlockColourSaver) block.asItem()).addSpriteDetails(details));
                        break;
                    }
                }
            });
            scanned = true;
        } catch (Exception e) {
            refreshColours();
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!MCRGBConfig.instance.alwaysShowToolTips) return;
        IItemBlockColourSaver item = (IItemBlockColourSaver) event.getItemStack().getItem();
        for (int i = 0; i < item.getLength(); i++) {
            List<String> strings = item.getSpriteDetails(i).getStrings();
            List<Integer> colours = item.getSpriteDetails(i).getTextColours();
            if (!strings.isEmpty()) {
                if (Screen.hasShiftDown()) {
                    for (int j = 0; j < strings.size(); j++) {
                        var text = Component.literal(strings.get(j)).withStyle(ChatFormatting.GRAY);
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

    public static void refreshColours() {
        if (client == null) return;
        //get top sprite of stone block default state
        TextureAtlasSprite defSprite = client.getModelManager().getBlockModelShaper().getBlockModel(Blocks.STONE.defaultBlockState()).getQuads(Blocks.STONE.defaultBlockState(), Direction.UP, RandomSource.create()).get(0).getSprite();
        //get id of the atlas containing above
        ResourceLocation atlas = defSprite.atlasLocation();
        //use atlas id to get OpenGL ID. Atlas contains ALL blocks
        int glID = client.getTextureManager().getTexture(atlas).getId();
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
            if (block.asItem().getDescriptionId().equals(Items.AIR.getDescriptionId())) return;
            ((IItemBlockColourSaver) block.asItem()).clearSpriteDetails();
            BlockColourStorage storage = new BlockColourStorage();
            totalBlocks += 1;
            Set<TextureAtlasSprite> sprites = new HashSet<>();
            block.getStateDefinition().getPossibleStates().forEach(state -> {
                try {
                    BakedModel model = client.getModelManager().getBlockModelShaper().getBlockModel(state);
                    sprites.add(model.getQuads(state, Direction.UP, RandomSource.create()).get(0).getSprite());
                    sprites.add(model.getQuads(state, Direction.DOWN, RandomSource.create()).get(0).getSprite());
                    sprites.add(model.getQuads(state, Direction.NORTH, RandomSource.create()).get(0).getSprite());
                    sprites.add(model.getQuads(state, Direction.SOUTH, RandomSource.create()).get(0).getSprite());
                    sprites.add(model.getQuads(state, Direction.EAST, RandomSource.create()).get(0).getSprite());
                    sprites.add(model.getQuads(state, Direction.WEST, RandomSource.create()).get(0).getSprite());
                    successes += 1;
                } catch (Exception e) {
                    fails += 1;
                }
            });
            if (sprites.isEmpty()) {
                return;
            }
            sprites.forEach(sprite -> {
                if (sprite.contents().name().getPath().equals("block/grass_block_side")) return;
                //get coords of sprite in atlas
                int spriteX = sprite.getX();
                int spriteY = sprite.getY();
                int spriteW = sprite.contents().width();
                int spriteH = sprite.contents().height();
                //convert coords to byte position
                int firstPixel = (spriteY * width + spriteX) * 4;
                ArrayList<ColourVector> rgbList = new ArrayList<>();
                int biomeColour = 0xFFFFFF;
                try {
                    biomeColour = client.getBlockColors().getColor(block.defaultBlockState(), null, null, 0);
                } catch (Exception e) {
                    LOGGER.warn("Could not find biome colour for block: {}. Please report this logfile to https://github.com/bacco-bacco/MCRGB/issues", block.getName());
                }
                //for each horizontal row in the sprite
                for (int row = 0; row < spriteH; row++) {
                    int firstInRow = firstPixel + row * width * 4;
                    //loop from first pixel in row to the sprite width.
                    //Note: Looping in increments of 4, because each pixel is 4 bytes. (R,G,B and A)
                    for (int pos = firstInRow; pos < firstInRow + 4 * spriteW; pos += 4) {
                        //retrieve bytes for RGBA values
                        //"& 0xFF" does logical and with 11111111. this extracts the last 8 bits, converting to unsigned int
                        int pixelColour = FastColor.ABGR32.color(pixels[pos + 3], pixels[pos] & 0xFF, pixels[pos + 1] & 0xFF, pixels[pos + 2] & 0xFF);
                        int alpha = FastColor.ABGR32.alpha(pixelColour);
                        if (biomeColour != -1 && (!block.defaultBlockState().is(Blocks.GRASS_BLOCK) || sprite.contents().name().getPath().equals("block/grass_block_top"))) {
                            pixelColour = FastColor.ARGB32.multiply(biomeColour, pixelColour);
                        }
                        //if the pixel is not fully transparent, add to the list
                        if (alpha > 0) {
                            ColourVector c = new ColourVector(FastColor.ABGR32.red(pixelColour), FastColor.ABGR32.green(pixelColour), FastColor.ABGR32.blue(pixelColour));
                            rgbList.add(c);
                        }
                    }
                }
                //Calculate the dominant colours
                Set<ColourGroup> colourGroups = groupColours(rgbList);
                if (colourGroups == null) return;

                //Add sprite name and each dominant colour to the IItemBlockColourSaver
                SpriteDetails spriteDetails = new SpriteDetails();
                String[] namesplit = sprite.contents().name().toString().split("/");
                spriteDetails.name = namesplit[namesplit.length - 1];
                colourGroups.forEach(cg -> {
                    spriteDetails.colourinfo.add(cg.meanColour);
                    spriteDetails.weights.add(cg.weight);
                });
                storage.block = block.asItem().getDescriptionId();
                storage.spriteDetails.add(spriteDetails);
            });
            storage.spriteDetails.forEach(details -> ((IItemBlockColourSaver) block.asItem()).addSpriteDetails(details));
            blockColourList.add(storage);
        });

        //Write arraylist to json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String blockColoursJson = gson.toJson(blockColourList);
        writeJson(blockColoursJson, "./mcrgb_forge_colours/", "file.json");
        client.player.displayClientMessage(Component.translatable("message.mcrgb_forge.reloaded"), false);
    }

    public static void savePalettes() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String blockColoursJson = gson.toJson(palettes);
        writeJson(blockColoursJson, "./mcrgb_forge_colours/", "palettes.json");
    }

    public static void loadPalettes() {
        List<Palette> loadedPalettes;
        TypeToken<List<Palette>> t = new TypeToken<>() {};
        try {
            loadedPalettes = new Gson().fromJson(readJson("./mcrgb_forge_colours/palettes.json"), t.getType());
        } catch (Exception e) {
            loadedPalettes = new ArrayList<>();
        }
        if (loadedPalettes == null) {
            loadedPalettes = new ArrayList<>();
        }

        palettes = loadedPalettes;

    }
}