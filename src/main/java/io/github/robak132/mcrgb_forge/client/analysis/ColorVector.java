package io.github.robak132.mcrgb_forge.client.analysis;

import lombok.Data;

@Data
public class ColorVector {

    private int r;
    private int g;
    private int b;

    public ColorVector(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    public ColorVector(String hex) {
        if (!hex.startsWith("#")) hex = "#" + hex;
        int rgb = Integer.parseInt(hex.substring(1), 16);
        this.r = (rgb >> 16) & 0xFF;
        this.g = (rgb >> 8) & 0xFF;
        this.b = rgb & 0xFF;
    }

    public ColorVector(int color) {
        this.r = (color >> 16) & 0xFF;
        this.g = (color >> 8) & 0xFF;
        this.b = color & 0xFF;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public String getHex() {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public int asInt() {
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    public int distanceSquared(ColorVector o) {
        int dr = this.r - o.r;
        int dg = this.g - o.g;
        int db = this.b - o.b;
        return dr*dr + dg*dg + db*db;
    }

    public ColorVector add(ColorVector o) {
        return new ColorVector(this.r + o.r, this.g + o.g, this.b + o.b);
    }

    public ColorVector div(int i) {
        return new ColorVector(this.r / i, this.g / i, this.b / i);
    }

    public static ColorVector fromHSV(int hue, int sat, int val) {
        float h = (hue % 360) / 60f;
        float s = sat / 100f;
        float v = val / 100f;

        float c = v * s;
        float x = c * (1 - Math.abs((h % 2) - 1));
        float m = v - c;

        float r1, g1, b1;

        if (h < 1) { r1 = c; g1 = x; b1 = 0; }
        else if (h < 2) { r1 = x; g1 = c; b1 = 0; }
        else if (h < 3) { r1 = 0; g1 = c; b1 = x; }
        else if (h < 4) { r1 = 0; g1 = x; b1 = c; }
        else if (h < 5) { r1 = x; g1 = 0; b1 = c; }
        else {           r1 = c; g1 = 0; b1 = x; }

        return new ColorVector(clamp(Math.round((r1 + m) * 255)), clamp(Math.round((g1 + m) * 255)), clamp(Math.round((b1 + m) * 255)));
    }

    public static ColorVector fromHSL(int hue, int sat, int light) {
        float h = (hue % 360) / 60f;
        float s = sat / 100f;
        float l = light / 100f;

        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h % 2) - 1));
        float m = l - c / 2;

        float r1, g1, b1;

        if (h < 1) { r1 = c; g1 = x; b1 = 0; }
        else if (h < 2) { r1 = x; g1 = c; b1 = 0; }
        else if (h < 3) { r1 = 0; g1 = c; b1 = x; }
        else if (h < 4) { r1 = 0; g1 = x; b1 = c; }
        else if (h < 5) { r1 = x; g1 = 0; b1 = c; }
        else {           r1 = c; g1 = 0; b1 = x; }

        return new ColorVector(clamp(Math.round((r1 + m) * 255)), clamp(Math.round((g1 + m) * 255)), clamp(Math.round((b1 + m) * 255)));
    }

    public int getHue() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float c = max - min;

        if (c == 0) return 0;

        float h;
        if (max == r) h = ((g - b) / c) % 6f;
        else if (max == g) h = ((b - r) / c) + 2f;
        else h = ((r - g) / c) + 4f;

        h *= 60f;
        if (h < 0) h += 360f;

        return Math.round(h);
    }

    public int getSatV() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        if (max == 0) return 0;

        float s = (max - min) / max;
        return Math.round(s * 100f);
    }

    public int getVal() {
        float max = Math.max(this.r, Math.max(this.g, this.b));
        return Math.round((max / 255f) * 100f);
    }

    public int getSatL() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        float l = (max + min) / 2f;
        float c = max - min;

        if (c == 0) return 0;

        float s = c / (1 - Math.abs(2 * l - 1));
        return Math.round(s * 100f);
    }

    public int getLight() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));

        float l = (max + min) / 2f;
        return Math.round(l * 100f);
    }
}
