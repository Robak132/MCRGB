package com.bacco;

import java.util.ArrayList;
import java.util.List;


public class SpriteDetails {
    public String name;
    public List<ColourVector> colourinfo = new ArrayList<>();
    public List<Integer> weights = new ArrayList<>();

    public List<String> getStrings() {
        List<String> strings = new ArrayList<>();
        strings.add(name + ":");
        for (int i = 0; i < colourinfo.size(); i++) {
            strings.add(colourinfo.get(i).getHex() + "  " + weights.get(i) + "%");
        }
        return strings;
    }

    public List<Integer> getTextColours() {
        List<Integer> colours = new ArrayList<>();
        colours.add(0xffffff);
        for (ColourVector colourVector : colourinfo) {
            String hex = colourVector.getHex();
            hex = hex.replace("#", "");
            int hexint = Integer.parseInt(hex, 16);
            colours.add(hexint);
        }
        return colours;
    }
}
