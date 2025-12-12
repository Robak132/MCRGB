package io.github.robak132.mcrgb_forge.colors;

import lombok.Data;

@Data
public final class LAB implements Color {
    private float L, a, b;
    private int alpha;

    public LAB(int alpha, float L, float a, float b) {
        this.alpha = alpha;
        this.L = L;
        this.a = a;
        this.b = b;
    }

    public LAB(LAB lab) {
        this.alpha = lab.alpha;
        this.L = lab.L;
        this.a = lab.a;
        this.b = lab.b;
    }

    public static float distanceSquared(LAB a, LAB b) {
        float dL = a.L - b.L;
        float da = a.a - b.a;
        float db = a.b - b.b;

        return dL * dL + da * da + db * db;
    }

    // sRGB 0..255 -> linear 0..1
    static float sRGBToLinear(float c) {
        c /= 255f;
        if (c <= 0.04045f) return c / 12.92f;
        return (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }

    // linear 0..1 -> sRGB 0..255
    static int linearToSRGBInt(float c) {
        float v;
        if (c <= 0.0031308f) v = 12.92f * c;
        else v = 1.055f * (float) Math.pow(c, 1.0 / 2.4) - 0.055f;
        int iv = Math.round(v * 255f);
        if (iv < 0) iv = 0;
        if (iv > 255) iv = 255;
        return iv;
    }

    @Override
    public RGB toRGB() {
        // OKLab -> l',m',s'
        float l_i = L + 0.3963377774f * a + 0.2158037573f * b;
        float m_i = L - 0.1055613458f * a - 0.0638541728f * b;
        float s_i = L - 0.0894841775f * a - 1.2914855480f * b;

        // cube
        float l = l_i * l_i * l_i;
        float m = m_i * m_i * m_i;
        float s = s_i * s_i * s_i;

        // LMS -> linear RGB
        float lr =  4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
        float lg = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
        float lb = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;

        // linear -> sRGB int
        int r_i = linearToSRGBInt(lr);
        int g_i = linearToSRGBInt(lg);
        int b_i = linearToSRGBInt(lb);
        return new RGB(alpha, r_i, g_i, b_i);
    }

    @Override
    public LAB toLAB() {
        return this;
    }

    public float L()   { return L; }
    public float a() { return a; }
    public float b()  { return b; }
    public int alpha() { return alpha; }

    @Override
    public Number[] values() {
        return new Number[] { alpha, L, a, b };
    }
}
