package io.github.robak132.mcrgb_forge.client.analysis;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Palette {

    List<ColorVector> colorList = new ArrayList<>();

    public void setColor(int i, ColorVector color) {
        colorList.set(i, color);
    }

    public ColorVector getColor(int i) {
        return colorList.get(i);
    }

    public void addColor(ColorVector color) {
        colorList.add(color);
    }
}
