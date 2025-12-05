package com.bacco.mixin;

import com.bacco.IItemBlockColourSaver;
import com.bacco.SpriteDetails;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

// Mixin, which adds one string to the Item class, and getter and setter functions. String colour stores the hexcode value of the block.
@Mixin(Item.class)
public abstract class BlockColourSaver implements IItemBlockColourSaver {
    private final ArrayList<SpriteDetails> spriteDetails = new ArrayList<>();
    private double score = 0;

    public SpriteDetails getSpriteDetails(int i) {
        if (this.spriteDetails.get(i) == null) {
            this.spriteDetails.add(new SpriteDetails());
        }
        return this.spriteDetails.get(i);
    }

    public void addSpriteDetails(SpriteDetails spriteDetails) {
        this.spriteDetails.add(spriteDetails);
    }

    public void clearSpriteDetails() {
        this.spriteDetails.clear();
    }

    public int getLength() {
        if (this.spriteDetails == null) return 0;
        return this.spriteDetails.size();
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
