package io.github.robak132.mcrgb_forge.client.analysis;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Storage class for block color data.
 * Holds all sprite-level color summaries for a single block.
 */
@Data
public class BlockColorStorage {

    /**
     * The block's description ID (e.g., "block.minecraft.stone").
     * This is used as a stable lookup key during load.
     */
    private String blockId;

    /**
     * The sprite-level coloring results extracted from the block model.
     */
    private final List<SpriteDetails> spriteDetails = new ArrayList<>();

    public BlockColorStorage() {}

    public BlockColorStorage(String blockId) {
        this.blockId = blockId;
    }

    public void addSpriteDetails(SpriteDetails details) {
        this.spriteDetails.add(details);
    }
}
