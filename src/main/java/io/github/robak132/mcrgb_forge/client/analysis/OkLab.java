package io.github.robak132.mcrgb_forge.client.analysis;

public final class OkLab {

    private OkLab() {}

    // sRGB 0..255 -> linear 0..1
    private static float srgbToLinear(float c) {
        c /= 255f;
        if (c <= 0.04045f) return c / 12.92f;
        return (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }

    // linear 0..1 -> sRGB 0..255
    private static int linearToSrgbInt(float c) {
        float v;
        if (c <= 0.0031308f) v = 12.92f * c;
        else v = 1.055f * (float) Math.pow(c, 1.0 / 2.4) - 0.055f;
        int iv = Math.round(v * 255f);
        if (iv < 0) iv = 0;
        if (iv > 255) iv = 255;
        return iv;
    }

    /**
     * Convert RGB (0..255) to OKLab coords [L, a, b] (floats).
     */
    public static float[] rgbToOkLab(int r, int g, int b) {
        // to linear RGB
        float lr = srgbToLinear(r);
        float lg = srgbToLinear(g);
        float lb = srgbToLinear(b);

        // linear RGB -> LMS
        float l = 0.4122214708f * lr + 0.5363325363f * lg + 0.0514459929f * lb;
        float m = 0.2119034982f * lr + 0.6806995451f * lg + 0.1073969566f * lb;
        float s = 0.0883024619f * lr + 0.2817188376f * lg + 0.6299787005f * lb;

        // nonlinear
        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        // LMS -> OKLab
        float L = 0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_;
        float a = 1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_;
        float b_ = 0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_;

        return new float[] { L, a, b_ };
    }

    /**
     * Convert OKLab [L,a,b] back to linear RGB and then sRGB ints (0..255).
     * Returns array {r,g,b}
     */
    public static int[] okLabToRgb(float L, float a, float b) {
        // OKLab -> l',m',s'
        float l_ = L + 0.3963377774f * a + 0.2158037573f * b;
        float m_ = L - 0.1055613458f * a - 0.0638541728f * b;
        float s_ = L - 0.0894841775f * a - 1.2914855480f * b;

        // cube
        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        // LMS -> linear RGB
        float lr =  4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
        float lg = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
        float lb = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;

        // linear -> sRGB int
        int ri = linearToSrgbInt(lr);
        int gi = linearToSrgbInt(lg);
        int bi = linearToSrgbInt(lb);
        return new int[] { ri, gi, bi };
    }

    /** OKLab Euclidean distance squared (cheap) */
    public static float distanceSq(float[] lab1, float[] lab2) {
        float dL = lab1[0] - lab2[0];
        float da = lab1[1] - lab2[1];
        float db = lab1[2] - lab2[2];
        return dL * dL + da * da + db * db;
    }
}
