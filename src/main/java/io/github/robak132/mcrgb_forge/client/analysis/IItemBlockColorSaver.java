package io.github.robak132.mcrgb_forge.client.analysis;

public interface IItemBlockColorSaver {
    SpriteDetails mcrgb_forge$getSpriteDetails(int i);
    void mcrgb_forge$addSpriteDetails(SpriteDetails spriteDetails);
    int mcrgb_forge$getLength();
    void mcrgb_forge$clearSpriteDetails();
    double mcrgb_forge$getScore();
    void mcrgb_forge$setScore(double score);
}
