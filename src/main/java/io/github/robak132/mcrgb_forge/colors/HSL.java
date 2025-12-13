package io.github.robak132.mcrgb_forge.colors;

public final class HSL extends Color {

    private final int h, s, l;

    public HSL(int alpha, int h, int s, int l) {
        this.alpha = alpha & 0xFF;
        this.h = Math.max(0, Math.min(360, h));
        this.s = Math.max(0, Math.min(100, s));
        this.l = Math.max(0, Math.min(100, l));
    }

    public HSL(int h, int s, int l) {
        this(255, h, s, l);
    }

    @Override
    public RGB toRGB() {
        float hNorm = this.h / 360f;
        float sNorm = this.s / 100f;
        float lNorm = this.l / 100f;

        float c = (1f - Math.abs(2f * lNorm - 1f)) * sNorm;
        float x = c * (1f - Math.abs((this.h / 60f) % 2f - 1f));
        float m = lNorm - c / 2f;

        float r, g, b;

        if (this.h < 60) {
            r = c;
            g = x;
            b = 0;
        } else if (this.h < 120) {
            r = x;
            g = c;
            b = 0;
        } else if (this.h < 180) {
            r = 0;
            g = c;
            b = x;
        } else if (this.h < 240) {
            r = 0;
            g = x;
            b = c;
        } else if (this.h < 300) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }

        int ri = Math.round((r + m) * 255);
        int gi = Math.round((g + m) * 255);
        int bi = Math.round((b + m) * 255);

        return new RGB(alpha, ri, gi, bi);
    }

    @Override
    public Number ch0() {
        return h;
    }

    @Override
    public Number ch1() {
        return s;
    }

    @Override
    public Number ch2() {
        return l;
    }

    @Override
    public HSL toHSL() {
        return this;
    }

    public int hue() {
        return h;
    }

    public int saturation() {
        return s;
    }

    public int lightness() {
        return l;
    }


}
