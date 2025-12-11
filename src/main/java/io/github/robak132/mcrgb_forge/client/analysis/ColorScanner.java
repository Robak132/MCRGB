package io.github.robak132.mcrgb_forge.client.analysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ColorScanner {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final RandomSource randomSource = RandomSource.create();
    private final int K_VALUE = 5;
    private final int MAX_ITERS = 8;
    private final int SAMPLE_LIMIT = 4096;

    public Map<String, SpriteDetails> refreshColors(List<Block> blocks) {
        return refreshColorsInternal(blocks);
    }
    private Map<String, SpriteDetails> refreshColorsInternal(List<Block> blocks) {
        Map<String, SpriteDetails> colorData = new HashMap<>();

        for (Block block : blocks) {
            Set<TextureAtlasSprite> sprites = getSprites(block);
            for (TextureAtlasSprite sprite : sprites) {
                List<ColorVector> pixels = getSpritePixels(sprite);
                if (!pixels.isEmpty()) {
                    List<SpriteColor> clustered = ColorClustering.kMeansOkLab(pixels, K_VALUE, MAX_ITERS, SAMPLE_LIMIT);
                    ResourceLocation spriteId = sprite.atlasLocation();
                    colorData.put(spriteId.toString(), new SpriteDetails(sprite.contents().name().getPath(), clustered));
                }
            }
        }
        return colorData;
    }

    public void saveColorData(Path filePath, Map<String, SpriteDetails> data) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        }
    }

    private Iterable<Direction> getDirectionsWithNull() {
        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
        directions.add(null);
        return directions;
    }

    private Set<TextureAtlasSprite> getSprites(Block block) {
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            IForgeBakedModel model = minecraft.getBlockRenderer().getBlockModelShaper().getBlockModel(state);
            for (Direction direction : getDirectionsWithNull()) {
                List<BakedQuad> quads = model.getQuads(state, direction, randomSource, ModelData.EMPTY, null);
                if (!quads.isEmpty()) {
                    sprites.add(quads.get(0).getSprite());
                }
            }
        }
        return sprites;
    }

    private List<ColorVector> getSpritePixels(TextureAtlasSprite sprite) {
        List<ColorVector> pixels = new ArrayList<>();
        int frameIndex = 0;
        int width = sprite.contents().width();
        int height = sprite.contents().height();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = sprite.getPixelRGBA(frameIndex, x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int a = (argb >> 24) & 0xFF;
                if (a > 0) pixels.add(new ColorVector(r, g, b));
            }
        }
        return pixels;
    }
}