package io.github.robak132.mcrgb_forge.colors;

public final class RGB extends Color {
    private final int r, g, b;

    public RGB(int r, int g, int b) {
        this(255, r, g, b);
    }

    public RGB(int alpha, int r, int g, int b) {
        this.alpha = alpha & 0xFF;
        this.r = r & 0xFF;
        this.g = g & 0xFF;
        this.b = b & 0xFF;
    }

    public RGB(int argb) {
        this.alpha = (argb >>> 24) & 0xFF;
        this.r = (argb >>> 16) & 0xFF;
        this.g = (argb >>> 8) & 0xFF;
        this.b = argb & 0xFF;
    }

    @Override
    public RGB toRGB() {
        return this;
    }

    public HSV toHSV() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h;
        if (delta == 0f) {
            h = 0f;
        } else if (max == r) {
            h = ((g - b) / delta) % 6f;
        } else if (max == g) {
            h = ((b - r) / delta) + 2f;
        } else {
            h = ((r - g) / delta) + 4f;
        }

        h *= 60f;
        if (h < 0f) {
            h += 360f;
        }

        float s = (max == 0f) ? 0f : (delta / max);
        float v = max;

        return new HSV(alpha, Math.round(h), Math.round(s * 100), Math.round(v * 100));
    }

    public HSL toHSL() {
        float r = this.r / 255f;
        float g = this.g / 255f;
        float b = this.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h;
        if (delta == 0f) {
            h = 0f;
        } else if (max == r) {
            h = ((g - b) / delta) % 6f;
        } else if (max == g) {
            h = ((b - r) / delta) + 2f;
        } else {
            h = ((r - g) / delta) + 4f;
        }

        h *= 60f;
        if (h < 0f) {
            h += 360f;
        }

        float l = (max + min) / 2f;
        float s = (delta == 0f) ? 0f : delta / (1f - Math.abs(2f * l - 1f));

        return new HSL(alpha, Math.round(h), Math.round(s * 100), Math.round(l * 100));
    }

    public OkLAB toOkLAB() {
        // to linear RGB
        float lr = OkLAB.sRGBToLinear(r);
        float lg = OkLAB.sRGBToLinear(g);
        float lb = OkLAB.sRGBToLinear(b);

        // linear RGB -> LMS
        float l = 0.4122214708f * lr + 0.5363325363f * lg + 0.0514459929f * lb;
        float m = 0.2119034982f * lr + 0.6806995451f * lg + 0.1073969566f * lb;
        float s = 0.0883024619f * lr + 0.2817188376f * lg + 0.6299787005f * lb;

        // nonlinear
        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        // LMS -> OKLab
        float L_i = 0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_;
        float a_i = 1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_;
        float b_1 = 0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_;

        return new OkLAB(alpha, L_i, a_i, b_1);
    }

    public int red() {
        return r;
    }

    public int green() {
        return g;
    }

    public int blue() {
        return b;
    }

    @Override
    public Number ch0() {
        return r;
    }

    @Override
    public Number ch1() {
        return g;
    }

    @Override
    public Number ch2() {
        return b;
    }

}
