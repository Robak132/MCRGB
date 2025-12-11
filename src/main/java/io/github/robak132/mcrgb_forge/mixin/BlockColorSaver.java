package io.github.robak132.mcrgb_forge.mixin;

import io.github.robak132.mcrgb_forge.client.analysis.IItemBlockColorSaver;
import io.github.robak132.mcrgb_forge.client.analysis.SpriteDetails;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import org.spongepowered.asm.mixin.Unique;

// Mixin, which adds one string to the Item class, and getter and setter functions. String color stores the hexcode value of the block.
@Mixin(Item.class)
public abstract class BlockColorSaver implements IItemBlockColorSaver {
    @Unique
    private final ArrayList<SpriteDetails> mcrgb_forge$spriteDetails = new ArrayList<>();

    @Unique
    private double mcrgb_forge$score = 0;

    @Unique
    public SpriteDetails mcrgb_forge$getSpriteDetails(int i) {
        if (this.mcrgb_forge$spriteDetails.get(i) == null) {
            this.mcrgb_forge$spriteDetails.add(new SpriteDetails());
        }
        return this.mcrgb_forge$spriteDetails.get(i);
    }

    @Unique
    public void mcrgb_forge$addSpriteDetails(SpriteDetails spriteDetails) {
        this.mcrgb_forge$spriteDetails.add(spriteDetails);
    }

    @Unique
    public void mcrgb_forge$clearSpriteDetails() {
        this.mcrgb_forge$spriteDetails.clear();
    }

    @Unique
    public int mcrgb_forge$getLength() {
        return this.mcrgb_forge$spriteDetails.size();
    }

    @Unique
    public double mcrgb_forge$getScore() {
        return this.mcrgb_forge$score;
    }

    @Unique
    public void mcrgb_forge$setScore(double score) {
        this.mcrgb_forge$score = score;
    }
}
