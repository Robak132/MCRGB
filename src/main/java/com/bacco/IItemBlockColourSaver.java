package com.bacco;

public interface IItemBlockColourSaver {
    SpriteDetails getSpriteDetails(int i);
    void addSpriteDetails(SpriteDetails spriteDetails);
    int getLength();
    void clearSpriteDetails();
    double getScore();
    void setScore(double score);
}
